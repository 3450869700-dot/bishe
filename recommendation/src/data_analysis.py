import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import seaborn as sns
import json
import os

# 确保目录存在
os.makedirs('data', exist_ok=True)

class DataAnalyzer:
    def __init__(self):
        self.products = None
        self.users = None
        self.interactions = None
    
    def generate_sample_data(self, product_count=1000, user_count=1000, interaction_count=10000):
        """生成样本数据"""
        # 实际商品数据库中的商品ID
        actual_product_ids = [49, 305, 299, 140, 195, 25, 289, 307, 8, 111, 244, 341, 387, 255, 73, 343, 316, 339, 44, 329, 169, 211, 258, 295, 216, 365, 164, 394, 27, 98, 207, 136, 261, 23, 367, 172, 264, 22, 202, 82, 326, 69, 366, 4, 375, 176, 230, 284, 377, 24]
        
        # 生成商品数据
        products = []
        categories = ['electronics', 'clothing', 'home', 'books', 'beauty']
        
        # 使用实际商品ID
        for product_id in actual_product_ids:
            product = {
                'product_id': product_id,
                'name': f'Product {product_id}',
                'category': np.random.choice(categories),
                'price': np.random.uniform(10, 1000),
                'rating': np.random.uniform(1, 5),
                'stock': np.random.randint(0, 100)
            }
            products.append(product)
        self.products = pd.DataFrame(products)
        
        # 生成用户数据
        users = []
        for i in range(1, user_count + 1):
            user = {
                'user_id': i,
                'age': np.random.randint(18, 65),
                'gender': np.random.choice(['male', 'female']),
                'occupation': np.random.choice(['student', 'professional', 'retired', 'other'])
            }
            users.append(user)
        self.users = pd.DataFrame(users)
        
        # 生成用户-商品交互数据
        interactions = []
        for i in range(interaction_count):
            interaction = {
                'user_id': np.random.randint(1, user_count + 1),
                'product_id': np.random.randint(1, product_count + 1),
                'interaction_type': np.random.choice(['view', 'add_to_cart', 'purchase'], p=[0.6, 0.2, 0.2]),
                'timestamp': pd.Timestamp.now() - pd.Timedelta(days=np.random.randint(0, 30))
            }
            interactions.append(interaction)
        self.interactions = pd.DataFrame(interactions)
        
        # 保存数据
        self.products.to_csv('data/products.csv', index=False)
        self.users.to_csv('data/users.csv', index=False)
        self.interactions.to_csv('data/interactions.csv', index=False)
        
        print(f"生成数据完成：")
        print(f"- 商品数: {product_count}")
        print(f"- 用户数: {user_count}")
        print(f"- 交互数: {interaction_count}")
    
    def load_data(self):
        """加载数据"""
        self.products = pd.read_csv('data/products.csv')
        self.users = pd.read_csv('data/users.csv')
        self.interactions = pd.read_csv('data/interactions.csv')
        self.interactions['timestamp'] = pd.to_datetime(self.interactions['timestamp'])
        print("数据加载完成")
    
    def analyze_data(self):
        """分析数据"""
        if self.products is None or self.users is None or self.interactions is None:
            self.load_data()
        
        print("\n=== 数据统计 ===")
        print(f"商品数量: {len(self.products)}")
        print(f"用户数量: {len(self.users)}")
        print(f"交互数量: {len(self.interactions)}")
        
        print("\n=== 商品类别分布 ===")
        print(self.products['category'].value_counts())
        
        print("\n=== 交互类型分布 ===")
        print(self.interactions['interaction_type'].value_counts())
        
        print("\n=== 价格分布 ===")
        print(f"平均价格: {self.products['price'].mean():.2f}")
        print(f"价格范围: {self.products['price'].min():.2f} - {self.products['price'].max():.2f}")
        
        print("\n=== 评分分布 ===")
        print(f"平均评分: {self.products['rating'].mean():.2f}")
        
        # 可视化
        self.visualize_data()
    
    def visualize_data(self):
        """可视化数据"""
        # 确保目录存在
        os.makedirs('data/visualizations', exist_ok=True)
        
        # 商品类别分布
        plt.figure(figsize=(10, 6))
        sns.countplot(x='category', data=self.products)
        plt.title('商品类别分布')
        plt.xticks(rotation=45)
        plt.savefig('data/visualizations/category_distribution.png')
        plt.close()
        
        # 交互类型分布
        plt.figure(figsize=(10, 6))
        sns.countplot(x='interaction_type', data=self.interactions)
        plt.title('交互类型分布')
        plt.savefig('data/visualizations/interaction_distribution.png')
        plt.close()
        
        # 价格分布
        plt.figure(figsize=(10, 6))
        sns.histplot(self.products['price'], bins=50)
        plt.title('价格分布')
        plt.savefig('data/visualizations/price_distribution.png')
        plt.close()
        
        # 评分分布
        plt.figure(figsize=(10, 6))
        sns.histplot(self.products['rating'], bins=20)
        plt.title('评分分布')
        plt.savefig('data/visualizations/rating_distribution.png')
        plt.close()
        
        print("可视化完成，结果保存在 data/visualizations/ 目录")

if __name__ == '__main__':
    analyzer = DataAnalyzer()
    analyzer.generate_sample_data()
    analyzer.analyze_data()
