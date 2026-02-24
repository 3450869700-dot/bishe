-- 优惠券主表：存储优惠券基础规则（纯PG标准语法，无内联索引/注释）
CREATE TABLE public.coupon (
    coupon_id BIGSERIAL PRIMARY KEY,
    coupon_name VARCHAR(100) NOT NULL,
    coupon_type SMALLINT NOT NULL, -- 1-满减券 2-无门槛券 3-折扣券
    face_value DECIMAL(10,2) DEFAULT 0.00, -- 满减/无门槛为金额，折扣券为折扣率（如0.9=9折）
    min_amount DECIMAL(10,2) DEFAULT 0.00, -- 使用门槛，无门槛设为0
    product_scope SMALLINT NOT NULL DEFAULT 1, -- 1-全平台 2-水蜜桃品类专享 3-指定商品
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    total_quantity INT NOT NULL DEFAULT 0, -- 总发行量，0为不限量
    remain_quantity INT NOT NULL DEFAULT 0, -- 剩余可领数量
    user_limit INT NOT NULL DEFAULT 1, -- 单用户限领数量
    is_valid SMALLINT NOT NULL DEFAULT 1, -- 1-有效 0-下架/作废
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 单独创建索引（PG标准方式，建表后独立执行）
CREATE INDEX IF NOT EXISTS idx_coupon_type ON public.coupon (coupon_type);
CREATE INDEX IF NOT EXISTS idx_coupon_valid ON public.coupon (is_valid);
CREATE INDEX IF NOT EXISTS idx_coupon_time ON public.coupon (start_time, end_time);

-- 为表和字段添加注释
COMMENT ON TABLE public.coupon IS '优惠券主表-存储优惠券基础规则';
COMMENT ON COLUMN public.coupon.coupon_id IS '优惠券唯一ID，自增';
COMMENT ON COLUMN public.coupon.coupon_name IS '优惠券名称（如满50减10水蜜桃专享券）';
COMMENT ON COLUMN public.coupon.coupon_type IS '优惠券类型：1-满减券 2-无门槛券 3-折扣券';
COMMENT ON COLUMN public.coupon.face_value IS '券面价值：满减/无门槛为金额，折扣券为0.0~1.0的折扣率';
COMMENT ON COLUMN public.coupon.min_amount IS '使用门槛（满X可用，无门槛设为0）';
COMMENT ON COLUMN public.coupon.product_scope IS '商品适用范围：1-全平台 2-水蜜桃品类专享 3-指定商品';
COMMENT ON COLUMN public.coupon.total_quantity IS '优惠券总发行量（0为不限量）';
COMMENT ON COLUMN public.coupon.remain_quantity IS '优惠券剩余可领数量';
COMMENT ON COLUMN public.coupon.user_limit IS '单用户限领数量';
COMMENT ON COLUMN public.coupon.is_valid IS '是否有效：1-有效 0-下架/作废';
COMMENT ON COLUMN public.coupon.create_time IS '创建时间';
COMMENT ON COLUMN public.coupon.update_time IS '修改时间';

-- 触发器：自动更新update_time字段
CREATE OR REPLACE FUNCTION public.update_coupon_time()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_coupon_update_time
BEFORE UPDATE ON public.coupon
FOR EACH ROW
EXECUTE FUNCTION public.update_coupon_time();