-- 商品表：存储商品信息（纯PG标准语法）
CREATE TABLE public.products (
    product_code BIGSERIAL PRIMARY KEY,
    name VARCHAR(200) NOT NULL, -- 商品名称
    weight VARCHAR(50), -- 商品重量
    packet VARCHAR(50), -- 商品包装
    variety VARCHAR(50), -- 商品品种
    grade VARCHAR(50), -- 商品等级
    specification_desc VARCHAR(100), -- 规格描述
    price VARCHAR(50) NOT NULL, -- 商品价格
    order_quantity VARCHAR(50), -- 起订量
    heat VARCHAR(50), -- 热度
    shop VARCHAR(100), -- 店铺名称
    address VARCHAR(200), -- 商品产地
    image TEXT, -- 商品图片URL
    rating DECIMAL(3,2), -- 商品评分
    stock INTEGER DEFAULT 100, -- 商品库存
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 单独创建索引
CREATE INDEX IF NOT EXISTS idx_products_name ON public.products (name);
CREATE INDEX IF NOT EXISTS idx_products_address ON public.products (address);
CREATE INDEX IF NOT EXISTS idx_products_shop ON public.products (shop);

-- 为表和字段添加注释
COMMENT ON TABLE public.products IS '商品表-存储商品信息';
COMMENT ON COLUMN public.products.product_code IS '商品唯一编码，自增';
COMMENT ON COLUMN public.products.name IS '商品名称';
COMMENT ON COLUMN public.products.weight IS '商品重量';
COMMENT ON COLUMN public.products.packet IS '商品包装';
COMMENT ON COLUMN public.products.variety IS '商品品种';
COMMENT ON COLUMN public.products.grade IS '商品等级';
COMMENT ON COLUMN public.products.specification_desc IS '规格描述';
COMMENT ON COLUMN public.products.price IS '商品价格';
COMMENT ON COLUMN public.products.order_quantity IS '起订量';
COMMENT ON COLUMN public.products.heat IS '热度';
COMMENT ON COLUMN public.products.shop IS '店铺名称';
COMMENT ON COLUMN public.products.address IS '商品产地';
COMMENT ON COLUMN public.products.image IS '商品图片URL';
COMMENT ON COLUMN public.products.rating IS '商品评分';
COMMENT ON COLUMN public.products.stock IS '商品库存';
COMMENT ON COLUMN public.products.create_time IS '创建时间';
COMMENT ON COLUMN public.products.update_time IS '修改时间';

-- 触发器：自动更新update_time字段
CREATE OR REPLACE FUNCTION public.update_products_time()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_products_update_time
BEFORE UPDATE ON public.products
FOR EACH ROW
EXECUTE FUNCTION public.update_products_time();
