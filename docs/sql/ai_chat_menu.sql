-- 添加 AI 助手菜单到系统管理或其他菜单下
-- 假设添加在系统工具目录下（parent_id = 3）

-- 先查看现有菜单结构
-- SELECT * FROM sys_menu ORDER BY id;

-- 添加 AI 助手菜单
INSERT INTO sys_menu (id, parent_id, menu_name, menu_type, path, component, perms, icon, sort, visible, status) VALUES
(400, 3, 'AI 助手', 2, 'ai-chat', 'ai-chat/index', 'ai:chat:list', 'MagicStick', 10, 1, 1);

SELECT setval('sys_menu_id_seq', (SELECT MAX(id) FROM sys_menu));

-- 给 admin 角色添加 AI 助手权限
INSERT INTO sys_role_menu (role_id, menu_id) VALUES (1, 400);

-- 给 common 角色也添加（如果需要）
-- INSERT INTO sys_role_menu (role_id, menu_id) VALUES (2, 400);
