from fastapi import FastAPI, HTTPException
import uvicorn
import pandas as pd
import joblib
import os

# 确保目录存在
os.makedirs('models', exist_ok=True)

class RecommendationService:
    def __init__(self):
        self.app = FastAPI(
            title="推荐系统API",
            description="商品推荐服务",
            version="1.0.0"
        )
        self.model = None
        self.load_model()
        self.setup_routes()
    
    def load_model(self):
        """加载模型"""
        try:
            self.user_similarity = joblib.load('models/user_similarity.pkl')
            self.item_similarity = joblib.load('models/item_similarity.pkl')
            self.user_item_matrix = joblib.load('models/user_item_matrix.pkl')
            self.products = joblib.load('models/products.pkl')
            print("模型加载完成")
        except Exception as e:
            print(f"模型加载失败: {e}")
            self.model = None
    
    def setup_routes(self):
        """设置路由"""
        @self.app.get("/recommend/{user_id}")
        async def recommend(user_id: int, top_n: int = 10):
            """为用户推荐商品"""
            if self.model is None:
                # 检查用户是否存在
                if user_id not in self.user_item_matrix.index:
                    # 新用户，返回热门商品
                    return self.get_popular_items(top_n)
                
                # 获取用户索引
                user_idx = self.user_item_matrix.index.get_loc(user_id)
                
                # 计算用户相似度
                user_similarities = self.user_similarity[user_idx]
                
                # 计算加权评分
                weighted_scores = np.dot(user_similarities, self.user_item_matrix.values)
                
                # 排除用户已交互过的商品
                user_interacted = self.user_item_matrix.loc[user_id].values
                weighted_scores[user_interacted > 0] = 0
                
                # 获取推荐商品索引
                top_indices = np.argsort(weighted_scores)[::-1][:top_n]
                
                # 获取推荐商品ID
                recommended_product_ids = self.user_item_matrix.columns[top_indices]
                
                # 获取推荐商品信息
                recommended_products = self.products[self.products['product_id'].isin(recommended_product_ids)]
                
                return recommended_products.to_dict('records')
            else:
                raise HTTPException(status_code=500, detail="模型未加载")
        
        @self.app.get("/popular")
        async def get_popular(top_n: int = 10):
            """获取热门商品"""
            return self.get_popular_items(top_n)
        
        @self.app.get("/health")
        async def health_check():
            """健康检查"""
            return {"status": "healthy"}
    
    def get_popular_items(self, top_n=10):
        """获取热门商品"""
        # 计算商品 popularity score
        item_scores = self.user_item_matrix.sum(axis=0)
        top_item_indices = np.argsort(item_scores)[::-1][:top_n]
        top_product_ids = self.user_item_matrix.columns[top_item_indices]
        
        popular_products = self.products[self.products['product_id'].isin(top_product_ids)]
        return popular_products.to_dict('records')
    
    def run(self, host="0.0.0.0", port=8001):
        """运行服务"""
        print(f"推荐服务启动在 http://{host}:{port}")
        uvicorn.run(self.app, host=host, port=port)

if __name__ == '__main__':
    import numpy as np
    service = RecommendationService()
    service.run()
