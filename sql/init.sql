-- =============================================
-- AI 生活优选 数据库初始化脚本
-- 用法：mysql -uroot -p < init.sql
-- =============================================
CREATE DATABASE IF NOT EXISTS ai_life DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
USE ai_life;

-- 用户表
DROP TABLE IF EXISTS sys_user;
CREATE TABLE sys_user (
  id                BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  username          VARCHAR(50)  NOT NULL UNIQUE COMMENT '登录账号',
  password          VARCHAR(100) NOT NULL COMMENT '密码(bcrypt)',
  nickname          VARCHAR(50)  NOT NULL COMMENT '昵称',
  phone             VARCHAR(20)  DEFAULT NULL COMMENT '手机号',
  avatar            VARCHAR(255) DEFAULT NULL COMMENT '头像',
  reputation_score  INT          DEFAULT 100 COMMENT '信誉分',
  role              VARCHAR(20)  DEFAULT 'USER' COMMENT '角色: USER/ADMIN',
  status            TINYINT      DEFAULT 1 COMMENT '状态: 1正常 0禁用',
  create_time       DATETIME     DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT '用户表';

-- 商家表
DROP TABLE IF EXISTS merchant;
CREATE TABLE merchant (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  name         VARCHAR(100)   NOT NULL COMMENT '商家名称',
  category     VARCHAR(50)    NOT NULL COMMENT '分类: 火锅/咖啡/餐厅等',
  address      VARCHAR(255)   DEFAULT NULL COMMENT '地址',
  longitude    DECIMAL(10, 6) DEFAULT NULL COMMENT '经度',
  latitude     DECIMAL(10, 6) DEFAULT NULL COMMENT '纬度',
  phone        VARCHAR(20)    DEFAULT NULL COMMENT '电话',
  rating       DECIMAL(2, 1)  DEFAULT 0 COMMENT '评分(0-5)',
  avg_price    DECIMAL(10, 2) DEFAULT 0 COMMENT '人均价格',
  open_hours   VARCHAR(50)    DEFAULT NULL COMMENT '营业时间',
  description  VARCHAR(500)   DEFAULT NULL COMMENT '简介',
  status       TINYINT        DEFAULT 1 COMMENT '状态: 1营业中 0停业',
  create_time  DATETIME       DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间'
) COMMENT '商家表';

-- 评价表
DROP TABLE IF EXISTS review;
CREATE TABLE review (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  merchant_id  BIGINT NOT NULL COMMENT '商家ID',
  user_id      BIGINT NOT NULL COMMENT '用户ID',
  content      VARCHAR(1000) DEFAULT NULL COMMENT '评价内容',
  rating       TINYINT       DEFAULT NULL COMMENT '评分(1-5)',
  images       VARCHAR(1000) DEFAULT NULL COMMENT '图片URL(逗号分隔)',
  status       TINYINT       DEFAULT 0 COMMENT '审核状态: 0待审核 1通过 2拒绝 3人工复审',
  audit_id     BIGINT        DEFAULT NULL COMMENT '审核记录ID',
  create_time  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_merchant (merchant_id)
) COMMENT '商家评价表';

-- 社区动态表
DROP TABLE IF EXISTS post;
CREATE TABLE post (
  id           BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  user_id      BIGINT NOT NULL COMMENT '用户ID',
  title        VARCHAR(100)  DEFAULT NULL COMMENT '标题',
  content      VARCHAR(2000) NOT NULL COMMENT '内容',
  images       VARCHAR(1000) DEFAULT NULL COMMENT '图片URL(逗号分隔)',
  status       TINYINT       DEFAULT 0 COMMENT '审核状态: 0待审核 1通过 2拒绝 3人工复审',
  audit_id     BIGINT        DEFAULT NULL COMMENT '审核记录ID',
  create_time  DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  KEY idx_user (user_id)
) COMMENT '社区动态表';

-- 审核记录表
DROP TABLE IF EXISTS audit_record;
CREATE TABLE audit_record (
  id                     BIGINT AUTO_INCREMENT PRIMARY KEY COMMENT '主键',
  target_type            VARCHAR(20)   NOT NULL COMMENT '目标类型: REVIEW/POST',
  target_id              BIGINT        NOT NULL COMMENT '目标ID',
  text_sensitive_words   VARCHAR(500)  DEFAULT NULL COMMENT '文本敏感词命中',
  image_result           VARCHAR(500)  DEFAULT NULL COMMENT '图像审核结果',
  reputation_result      VARCHAR(500)  DEFAULT NULL COMMENT '用户信誉校验结果',
  llm_result             TEXT          DEFAULT NULL COMMENT 'LLM 语义审核输出',
  risk_level             VARCHAR(20)   DEFAULT 'LOW' COMMENT '风险等级: LOW/MEDIUM/HIGH',
  audit_status           VARCHAR(20)   NOT NULL COMMENT '审核状态: APPROVED/REJECTED/MANUAL',
  review_comment         VARCHAR(500)  DEFAULT NULL COMMENT '人工复审意见',
  reviewer_id            BIGINT        DEFAULT NULL COMMENT '复审人ID',
  create_time            DATETIME      DEFAULT CURRENT_TIMESTAMP COMMENT '审核时间',
  review_time            DATETIME      DEFAULT NULL COMMENT '复审时间',
  KEY idx_status (audit_status),
  KEY idx_target (target_type, target_id)
) COMMENT '内容审核记录表';

-- ============ 演示数据：商家（北京坐标） ============
INSERT INTO merchant (name, category, address, longitude, latitude, phone, rating, avg_price, open_hours, description) VALUES
('老北京铜锅涮肉', '火锅', '东城区东直门大街 88 号', 116.4373, 39.9411, '010-88880001', 4.8, 120.00, '10:00-22:00', '正宗老北京铜锅涮肉，手切鲜羊肉'),
('川香麻辣烫', '小吃', '朝阳区望京西园 3 区 12 号楼', 116.4734, 39.9960, '010-88880002', 4.5, 45.00, '09:00-22:00', '秘制汤底，麻辣过瘾'),
('星巴克臻选咖啡', '咖啡', '朝阳区三里屯太古里南区', 116.4551, 39.9347, '010-88880003', 4.6, 55.00, '07:00-23:00', '臻选咖啡豆，手冲体验'),
('巷口小炉烧烤', '烧烤', '海淀区学院路 29 号', 116.3480, 39.9890, '010-88880004', 4.7, 85.00, '17:00-02:00', '炭火烧烤，深夜食堂'),
('一点点奶茶', '奶茶', '西城区西单北大街 120 号', 116.3740, 39.9130, '010-88880005', 4.4, 20.00, '10:00-22:00', '招牌波霸奶茶'),
('全聚德烤鸭店', '餐厅', '东城区前门大街 30 号', 116.3982, 39.8994, '010-88880006', 4.9, 180.00, '11:00-21:30', '百年烤鸭老字号'),
('沙县小吃', '快餐', '丰台区方庄芳城园 1 区', 116.4280, 39.8650, '010-88880007', 4.2, 25.00, '07:00-21:00', '实惠快餐，拌面蒸饺'),
('罗森便利店', '便利店', '海淀区中关村大街 1 号', 116.3160, 39.9830, '010-88880008', 4.3, 30.00, '24小时', '24 小时便利店，关东煮'),
('胡同里书店', '书店', '东城区南锣鼓巷 15 号', 116.4020, 39.9370, '010-88880009', 4.8, 40.00, '10:00-21:00', '独立书店，文艺打卡地'),
('蓝色港湾精品日料', '餐厅', '朝阳区朝阳公园路 6 号', 116.4800, 39.9460, '010-88880010', 4.7, 260.00, '11:00-22:00', '日式料理，刺身新鲜');
