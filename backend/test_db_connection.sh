#!/bin/bash

# PostgreSQL数据库连接测试脚本
# 使用方法: 在命令行中运行 ./test_db_connection.sh

echo "================================"
echo "PostgreSQL数据库连接测试"
echo "================================"
echo ""

# 测试数据库连接
echo "1. 测试PostgreSQL服务状态..."
if command -v pg_isready &> /dev/null; then
    pg_isready -h localhost -p 5432
    if [ $? -eq 0 ]; then
        echo "   ✓ PostgreSQL服务正在运行"
    else
        echo "   ✗ PostgreSQL服务未运行或无法连接"
    fi
else
    echo "   pg_isready命令不可用，尝试使用其他方法测试..."
fi

echo ""
echo "2. 测试数据库连接..."
echo "   正在尝试连接数据库..."

# 测试连接（需要安装psql命令）
if command -v psql &> /dev/null; then
    PGPASSWORD=123456 psql -h localhost -U postgres -d testpostgres -c "SELECT 1 as test;" 2>&1
    if [ $? -eq 0 ]; then
        echo "   ✓ 数据库连接成功！"
    else
        echo "   ✗ 数据库连接失败！"
    fi
else
    echo "   psql命令不可用，跳过数据库连接测试"
fi

echo ""
echo "3. 检查数据库列表..."
if command -v psql &> /dev/null; then
    PGPASSWORD=123456 psql -h localhost -U postgres -c "\l" 2>&1 | grep -i testpostgres
    if [ $? -eq 0 ]; then
        echo "   ✓ testpostgres数据库存在"
    else
        echo "   ✗ testpostgres数据库不存在，需要创建"
        echo "   创建命令: CREATE DATABASE testpostgres;"
    fi
else
    echo "   psql命令不可用，跳过数据库列表检查"
fi

echo ""
echo "================================"
echo "测试完成"
echo "================================