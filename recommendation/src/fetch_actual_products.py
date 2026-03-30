import requests
import pandas as pd
import numpy as np
import os

# 确保目录存在
os.makedirs('data', exist_ok=True)

def fetch_products():
    """从实际商品数据库中获取商品数据"""
    print("开始获取实际商品数据...")
    
    # 获取商品列表
    url = "http://localhost:8080/api/products"
    params = {
        "page": 0,
        "pageSize": 1000  # 假设最多1000个商品
    }
    
    response = requests.get(url, params=params)
    if response.status_code != 200:
        print(f"获取商品数据失败: {response.status_code}")
        return None
    
    data = response.json()
    if data.get("code") != 0:
        print(f"获取商品数据失败: {data.get('msg')}")
        return None
    
    products_data = data.get("data", {}).get("result", [])
    print(f"获取到 {len(products_data)} 个商品")
    
    # 转换为DataFrame
    products = []
    for item in products_data:
        product = {
            "product_id": item.get("id"),
            "name": item.get("name"),
            "category": "general",  # 假设所有商品都属于general类别
            "price": item.get("price_num", 0),
            "rating": np.random.uniform(3, 5),  # 随机生成评分
            "stock": 100  # 假设库存为100
        }
        products.append(product)
    
    products_df = pd.DataFrame(products)
    print(f"商品数据转换完成，共 {len(products_df)} 个商品")
    
    # 保存商品数据
    products_df.to_csv('data/actual_products.csv', index=False)
    print("商品数据保存到 data/actual_products.csv")
    
    return products_df

def generate_interactions(products_df, user_count=100, interaction_count=5000):
    """生成用户-商品交互数据"""
    print("开始生成用户-商品交互数据...")
    
    # 生成用户数据
    users = []
    for i in range(1, user_count + 1):
        user = {
            "user_id": i,
            "age": np.random.randint(18, 65),
            "gender": np.random.choice(['male', 'female']),
            "occupation": np.random.choice(['student', 'professional', 'retired', 'other'])
        }
        users.append(user)
    users_df = pd.DataFrame(users)
    
    # 生成用户-商品交互数据
    interactions = []
    product_ids = products_df['product_id'].tolist()
    
    if not product_ids:
        print("没有商品数据，无法生成交互数据")
        return None
    
    for i in range(interaction_count):
        interaction = {
            "user_id": np.random.randint(1, user_count + 1),
            "product_id": np.random.choice(product_ids),
            "interaction_type": np.random.choice(['view', 'add_to_cart', 'purchase'], p=[0.6, 0.2, 0.2]),
            "timestamp": pd.Timestamp.now() - pd.Timedelta(days=np.random.randint(0, 30))
        }
        interactions.append(interaction)
    interactions_df = pd.DataFrame(interactions)
    
    # 保存数据
    users_df.to_csv('data/users.csv', index=False)
    interactions_df.to_csv('data/interactions.csv', index=False)
    
    print(f"生成交互数据完成：")
    print(f"- 用户数: {user_count}")
    print(f"- 交互数: {interaction_count}")
    
    return users_df, interactions_df

if __name__ == '__main__':
    # 获取实际商品数据
    products_df = fetch_products()
    
    if products_df is not None and len(products_df) > 0:
        # 生成交互数据
        generate_interactions(products_df)
    else:
        print("无法获取商品数据，无法生成交互数据")
