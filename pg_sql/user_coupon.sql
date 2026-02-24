-- 用户优惠券表：存储用户领用/使用/过期记录（纯PG标准语法，无WHERE条件）
CREATE TABLE public.user_coupon (
    user_coupon_id BIGSERIAL PRIMARY KEY,
    user_id BIGINT NOT NULL, -- 领用用户ID
    coupon_id BIGINT NOT NULL, -- 关联优惠券主表ID
    coupon_code VARCHAR(32) UNIQUE NOT NULL, -- 优惠券唯一核销码（下单验证）
    use_status SMALLINT NOT NULL DEFAULT 1, -- 1-未使用 2-已使用 3-已过期
    receive_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP, -- 领用时间
    use_time TIMESTAMP WITH TIME ZONE DEFAULT NULL, -- 使用时间（核销时更新）
    order_id BIGINT DEFAULT NULL, -- 关联订单ID（使用后绑定，便于溯源）
    -- 外键约束：主表优惠券删除，用户领用记录自动删除
    CONSTRAINT fk_user_coupon_coupon FOREIGN KEY (coupon_id) REFERENCES public.coupon (coupon_id) ON DELETE CASCADE
);

-- 单独创建索引（高频查询场景优化）
CREATE INDEX IF NOT EXISTS idx_user_coupon_user ON public.user_coupon (user_id);
CREATE INDEX IF NOT EXISTS idx_user_coupon_status ON public.user_coupon (use_status);
CREATE INDEX IF NOT EXISTS idx_user_coupon_coupon ON public.user_coupon (coupon_id);
-- 联合索引：优化“查询用户某状态的某张券”（替代原WHERE条件的唯一约束）
CREATE INDEX IF NOT EXISTS idx_user_coupon_user_coupon_status ON public.user_coupon (user_id, coupon_id, use_status);

-- 为表和字段、约束添加注释
COMMENT ON TABLE public.user_coupon IS '用户优惠券表-存储用户领用、使用、过期记录';
COMMENT ON COLUMN public.user_coupon.user_coupon_id IS '用户优惠券唯一ID，自增';
COMMENT ON COLUMN public.user_coupon.user_id IS '领用用户ID';
COMMENT ON COLUMN public.user_coupon.coupon_id IS '关联优惠券主表ID';
COMMENT ON COLUMN public.user_coupon.coupon_code IS '优惠券唯一核销码，下单时验证唯一性';
COMMENT ON COLUMN public.user_coupon.use_status IS '使用状态：1-未使用 2-已使用 3-已过期';
COMMENT ON COLUMN public.user_coupon.receive_time IS '领用时间';
COMMENT ON COLUMN public.user_coupon.use_time IS '使用时间（核销时更新）';
COMMENT ON COLUMN public.user_coupon.order_id IS '关联订单ID（使用后绑定，便于售后溯源）';
COMMENT ON CONSTRAINT fk_user_coupon_coupon ON public.user_coupon IS '外键约束：关联优惠券主表，主表记录删除则用户领用记录同步删除';