-- 收货地址表：存储用户收货地址信息（纯PG标准语法）
CREATE TABLE public.shipping_address (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL, -- 关联用户ID
    link_man VARCHAR(50) NOT NULL, -- 收货人姓名
    mobile VARCHAR(11) NOT NULL, -- 收货人手机号
    province_id VARCHAR(10), -- 省份编码
    province_str VARCHAR(50), -- 省份名称
    city_id VARCHAR(10), -- 城市编码
    city_str VARCHAR(50), -- 城市名称
    district_id VARCHAR(10), -- 区县编码
    area_str VARCHAR(50), -- 区县名称
    address VARCHAR(200) NOT NULL, -- 详细地址
    is_default SMALLINT NOT NULL DEFAULT 0, -- 是否为默认地址：1-是 0-否
    is_valid SMALLINT NOT NULL DEFAULT 1, -- 是否有效：1-有效 0-删除
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    -- 外键约束：关联用户表
    CONSTRAINT fk_shipping_address_user FOREIGN KEY (user_id) REFERENCES public."user" (user_id) ON DELETE CASCADE
);

-- 单独创建索引
CREATE INDEX IF NOT EXISTS idx_shipping_address_user ON public.shipping_address (user_id);
CREATE INDEX IF NOT EXISTS idx_shipping_address_valid ON public.shipping_address (is_valid);
CREATE INDEX IF NOT EXISTS idx_shipping_address_default ON public.shipping_address (is_default);

-- 为表和字段添加注释
COMMENT ON TABLE public.shipping_address IS '收货地址表-存储用户收货地址信息';
COMMENT ON COLUMN public.shipping_address.id IS '地址唯一ID，自增';
COMMENT ON COLUMN public.shipping_address.user_id IS '关联用户ID';
COMMENT ON COLUMN public.shipping_address.link_man IS '收货人姓名';
COMMENT ON COLUMN public.shipping_address.mobile IS '收货人手机号';
COMMENT ON COLUMN public.shipping_address.province_id IS '省份编码';
COMMENT ON COLUMN public.shipping_address.province_str IS '省份名称';
COMMENT ON COLUMN public.shipping_address.city_id IS '城市编码';
COMMENT ON COLUMN public.shipping_address.city_str IS '城市名称';
COMMENT ON COLUMN public.shipping_address.district_id IS '区县编码';
COMMENT ON COLUMN public.shipping_address.area_str IS '区县名称';
COMMENT ON COLUMN public.shipping_address.address IS '详细地址';
COMMENT ON COLUMN public.shipping_address.is_default IS '是否为默认地址：1-是 0-否';
COMMENT ON COLUMN public.shipping_address.is_valid IS '是否有效：1-有效 0-删除';
COMMENT ON COLUMN public.shipping_address.create_time IS '创建时间';
COMMENT ON COLUMN public.shipping_address.update_time IS '修改时间';
COMMENT ON CONSTRAINT fk_shipping_address_user ON public.shipping_address IS '外键约束：关联用户表，用户删除则地址记录同步删除';

-- 触发器：自动更新update_time字段
CREATE OR REPLACE FUNCTION public.update_shipping_address_time()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_shipping_address_update_time
BEFORE UPDATE ON public.shipping_address
FOR EACH ROW
EXECUTE FUNCTION public.update_shipping_address_time();
