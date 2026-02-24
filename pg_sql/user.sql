-- 用户表：存储用户信息和权限（纯PG标准语法，无内联索引/注释）
CREATE TABLE public."user" (
    user_id BIGSERIAL PRIMARY KEY,
    phone VARCHAR(11) UNIQUE NOT NULL, -- 手机号，唯一
    password VARCHAR(100) NOT NULL, -- 密码（建议存储加密后的密码）
    user_type SMALLINT NOT NULL DEFAULT 1, -- 用户类型：1-普通消费者 2-商户 3-管理员
    username VARCHAR(50) DEFAULT NULL, -- 用户名
    is_valid SMALLINT NOT NULL DEFAULT 1, -- 1-有效 0-禁用
    create_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- 单独创建索引（PG标准方式，建表后独立执行）
CREATE INDEX IF NOT EXISTS idx_user_phone ON public."user" (phone);
CREATE INDEX IF NOT EXISTS idx_user_type ON public."user" (user_type);
CREATE INDEX IF NOT EXISTS idx_user_valid ON public."user" (is_valid);

-- 为表和字段添加注释
COMMENT ON TABLE public."user" IS '用户表-存储用户信息和权限';
COMMENT ON COLUMN public."user".user_id IS '用户唯一ID，自增';
COMMENT ON COLUMN public."user".phone IS '手机号，唯一';
COMMENT ON COLUMN public."user".password IS '密码（建议存储加密后的密码）';
COMMENT ON COLUMN public."user".user_type IS '用户类型：1-普通消费者 2-商户 3-管理员';
COMMENT ON COLUMN public."user".username IS '用户名';
COMMENT ON COLUMN public."user".is_valid IS '是否有效：1-有效 0-禁用';
COMMENT ON COLUMN public."user".create_time IS '创建时间';
COMMENT ON COLUMN public."user".update_time IS '修改时间';

-- 触发器：自动更新update_time字段
CREATE OR REPLACE FUNCTION public.update_user_time()
RETURNS TRIGGER AS $$
BEGIN
    NEW.update_time = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trigger_user_update_time
BEFORE UPDATE ON public."user"
FOR EACH ROW
EXECUTE FUNCTION public.update_user_time();

-- 插入默认管理员账户（万能账户）
INSERT INTO public."user" (phone, password, user_type, username, is_valid)
VALUES ('13800138000', 'admin123', 3, '管理员', 1)
ON CONFLICT (phone) DO NOTHING;