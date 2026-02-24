-- 购物车表：存储用户购物车商品（纯PG标准语法）
CREATE TABLE public.shopping_cart (
    cart_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL, -- 关联用户ID
    product_code BIGINT NOT NULL, -- 商品编码，关联products表的product_code
    product_price DECIMAL(10,2) NOT NULL, -- 商品价格
    maturity VARCHAR(20) DEFAULT NULL, -- 成熟度
    product_num INT NOT NULL DEFAULT 1, -- 商品数量
    is_checked SMALLINT NOT NULL DEFAULT 1, -- 1-已选中 0-未选中
    is_valid SMALLINT NOT NULL DEFAULT 1, -- 1-有效 0-删除
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 外键约束：关联用户表
    CONSTRAINT fk_shopping_cart_user FOREIGN KEY (user_id) REFERENCES public."user" (user_id) ON DELETE CASCADE,
    -- 外键约束：关联商品表
    CONSTRAINT fk_shopping_cart_product FOREIGN KEY (product_code) REFERENCES public.products (product_code) ON DELETE CASCADE,
    -- 唯一约束：用户+商品组合唯一
    CONSTRAINT uk_user_product UNIQUE (user_id, product_code)
);

-- 单独创建索引
CREATE INDEX IF NOT EXISTS idx_shopping_cart_user ON public.shopping_cart (user_id);
CREATE INDEX IF NOT EXISTS idx_shopping_cart_valid ON public.shopping_cart (is_valid);

-- 为表和字段添加注释
COMMENT ON TABLE public.shopping_cart IS '购物车表-存储用户购物车商品';
COMMENT ON COLUMN public.shopping_cart.cart_id IS '购物车记录唯一ID，自增';
COMMENT ON COLUMN public.shopping_cart.user_id IS '关联用户ID';
COMMENT ON COLUMN public.shopping_cart.product_code IS '商品编码，关联products表的product_code';
COMMENT ON COLUMN public.shopping_cart.product_price IS '商品价格';
COMMENT ON COLUMN public.shopping_cart.maturity IS '成熟度';
COMMENT ON COLUMN public.shopping_cart.product_num IS '商品数量';
COMMENT ON COLUMN public.shopping_cart.is_checked IS '是否选中：1-已选中 0-未选中';
COMMENT ON COLUMN public.shopping_cart.is_valid IS '是否有效：1-有效 0-删除';
COMMENT ON COLUMN public.shopping_cart.create_time IS '创建时间';
COMMENT ON COLUMN public.shopping_cart.update_time IS '修改时间';
COMMENT ON CONSTRAINT fk_shopping_cart_user ON public.shopping_cart IS '外键约束：关联用户表，用户删除则购物车记录同步删除';
COMMENT ON CONSTRAINT fk_shopping_cart_product ON public.shopping_cart IS '外键约束：关联商品表，商品删除则购物车记录同步删除';
COMMENT ON CONSTRAINT uk_user_product ON public.shopping_cart IS '唯一约束：用户+商品组合唯一，避免重复添加';

-- 触发器：自动更新update_time字段
CREATE OR REPLACE FUNCTION public.update_shopping_cart_time()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_shopping_cart_update_time
BEFORE UPDATE ON public.shopping_cart
FOR EACH ROW
EXECUTE FUNCTION public.update_shopping_cart_time();

-- 封装成函数：添加商品（指定数量）
CREATE OR REPLACE FUNCTION public.add_cart_product(
    p_user_id BIGINT,
    p_product_code BIGINT,
    p_product_price DECIMAL(10,2),
    p_maturity VARCHAR(20),
    p_add_num INT DEFAULT 1 -- 要添加的数量，默认+1
) RETURNS VOID AS $$
BEGIN
    INSERT INTO public.shopping_cart (
        user_id, product_code, product_price, maturity
    ) VALUES (
        p_user_id, p_product_code, p_product_price, p_maturity
    )
    ON CONFLICT (user_id, product_code)
    DO UPDATE SET
        product_num = shopping_cart.product_num + p_add_num,
        update_time = CURRENT_TIMESTAMP;
END;
$$ LANGUAGE plpgsql;

-- 调用函数：给1号用户添加2件101号商品
SELECT public.add_cart_product(1, 101, 79.00, '9分熟', 2);

-- 调用函数：给1号用户再添加1件101号商品（数量+1）
SELECT public.add_cart_product(1, 101, 79.00, '9分熟');