import pandas as pd
import numpy as np
from sklearn.metrics.pairwise import cosine_similarity
from sklearn.preprocessing import StandardScaler
import joblib
import os

# 确保目录存在
os.makedirs('models', exist_ok=True)

class RecommendationModel:
    def __init__(self):
        self.user_item_matrix = None
        self.user_similarity = None
        self.item_similarity = None
        self.products = None
    
    def load_data(self):
        """加载数据"""
        # 尝试加载实际商品数据，如果不存在则使用生成的数据
        try:
            self.products = pd.read_csv('data/actual_products.csv')
            print("加载实际商品数据")
        except FileNotFoundError:
            self.products = pd.read_csv('data/products.csv')
            print("加载生成的商品数据")
        
        interactions = pd.read_csv('data/interactions.csv')
        return interactions
    
    def create_user_item_matrix(self, interactions):
        """创建用户-物品矩阵"""
        # 计算每个用户对每个商品的交互分数
        # 购买: 3分, 加入购物车: 2分, 浏览: 1分
        score_map = {'view': 1, 'add_to_cart': 2, 'purchase': 3}
        interactions['score'] = interactions['interaction_type'].map(score_map)
        
        # 创建用户-物品矩阵
        user_item_matrix = interactions.pivot_table(
            index='user_id', 
            columns='product_id', 
            values='score', 
            fill_value=0
        )
        
        self.user_item_matrix = user_item_matrix
        print(f"用户-物品矩阵形状: {user_item_matrix.shape}")
        return user_item_matrix
    
    def train(self):
        """训练模型"""
        interactions = self.load_data()
        user_item_matrix = self.create_user_item_matrix(interactions)
        
        # 计算用户相似度
        self.user_similarity = cosine_similarity(user_item_matrix)
        
        # 计算物品相似度
        self.item_similarity = cosine_similarity(user_item_matrix.T)
        
        # 保存模型
        joblib.dump(self.user_similarity, 'models/user_similarity.pkl')
        joblib.dump(self.item_similarity, 'models/item_similarity.pkl')
        joblib.dump(self.user_item_matrix, 'models/user_item_matrix.pkl')
        joblib.dump(self.products, 'models/products.pkl')
        
        print("模型训练完成并保存")
    
    def load_model(self):
        """加载模型"""
        self.user_similarity = joblib.load('models/user_similarity.pkl')
        self.item_similarity = joblib.load('models/item_similarity.pkl')
        self.user_item_matrix = joblib.load('models/user_item_matrix.pkl')
        self.products = joblib.load('models/products.pkl')
        print("模型加载完成")
    
    def recommend_items(self, user_id, top_n=10):
        """为用户推荐商品"""
        if self.user_item_matrix is None:
            self.load_model()
        
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
        
        return recommended_products
    
    def get_popular_items(self, top_n=10):
        """获取热门商品"""
        if self.user_item_matrix is None:
            self.load_model()
        
        # 计算商品 popularity score
        item_scores = self.user_item_matrix.sum(axis=0)
        top_item_indices = np.argsort(item_scores)[::-1][:top_n]
        top_product_ids = self.user_item_matrix.columns[top_item_indices]
        
        popular_products = self.products[self.products['product_id'].isin(top_product_ids)]
        return popular_products
    
    def evaluate(self):
        """评估模型"""
        if self.user_item_matrix is None:
            self.load_model()
        
        # 简单评估：计算平均推荐分数
        total_score = 0
        count = 0
        
        for user_id in self.user_item_matrix.index[:100]:  # 只评估前100个用户
            recommendations = self.recommend_items(user_id, top_n=5)
            if not recommendations.empty:
                total_score += recommendations['rating'].mean()
                count += 1
        
        if count > 0:
            avg_score = total_score / count
            print(f"平均推荐评分: {avg_score:.2f}")
        else:
            print("评估失败：没有推荐结果")

if __name__ == '__main__':
    model = RecommendationModel()
    model.train()
    model.evaluate()
    
    # 测试推荐
    print("\n=== 测试推荐 ===")
    user_id = 1
    recommendations = model.recommend_items(user_id, top_n=5)
    print(f"为用户 {user_id} 推荐的商品:")
    print(recommendations[['product_id', 'name', 'category', 'price', 'rating']])
    
    # 测试热门商品
    print("\n=== 热门商品 ===")
    popular_items = model.get_popular_items(top_n=5)
    print(popular_items[['product_id', 'name', 'category', 'price', 'rating']])
