from fastapi import FastAPI, HTTPException
import uvicorn
import random

# 实际商品数据库中的商品ID
actual_product_ids = [49, 305, 299, 140, 195, 25, 289, 307, 8, 111, 244, 341, 387, 255, 73, 343, 316, 339, 44, 329, 169, 211, 258, 295, 216, 365, 164, 394, 27, 98, 207, 136, 261, 23, 367, 172, 264, 22, 202, 82, 326, 69, 366, 4, 375, 176, 230, 284, 377, 24]

class SimpleRecommendationService:
    def __init__(self):
        self.app = FastAPI(
            title="推荐系统API",
            description="商品推荐服务",
            version="1.0.0"
        )
        self.setup_routes()
    
    def setup_routes(self):
        """设置路由"""
        @self.app.get("/recommend/{user_id}")
        async def recommend(user_id: int, top_n: int = 10):
            """为用户推荐商品"""
            # 随机从实际商品ID中选择top_n个商品
            recommended_ids = random.sample(actual_product_ids, min(top_n, len(actual_product_ids)))
            
            # 构建推荐结果
            recommendations = []
            for product_id in recommended_ids:
                recommendations.append({
                    "product_id": product_id,
                    "name": f"Product {product_id}",
                    "category": "general",
                    "price": round(random.uniform(10, 1000), 2),
                    "rating": round(random.uniform(3, 5), 2),
                    "stock": random.randint(0, 100)
                })
            
            return recommendations
        
        @self.app.get("/popular")
        async def get_popular(top_n: int = 10):
            """获取热门商品"""
            # 随机从实际商品ID中选择top_n个商品
            popular_ids = random.sample(actual_product_ids, min(top_n, len(actual_product_ids)))
            
            # 构建热门商品结果
            popular_items = []
            for product_id in popular_ids:
                popular_items.append({
                    "product_id": product_id,
                    "name": f"Product {product_id}",
                    "category": "general",
                    "price": round(random.uniform(10, 1000), 2),
                    "rating": round(random.uniform(3, 5), 2),
                    "stock": random.randint(0, 100)
                })
            
            return popular_items
        
        @self.app.get("/health")
        async def health_check():
            """健康检查"""
            return {"status": "healthy"}
    
    def run(self, host="0.0.0.0", port=8001):
        """运行服务"""
        print(f"推荐服务启动在 http://{host}:{port}")
        uvicorn.run(self.app, host=host, port=port)

if __name__ == '__main__':
    service = SimpleRecommendationService()
    service.run()
