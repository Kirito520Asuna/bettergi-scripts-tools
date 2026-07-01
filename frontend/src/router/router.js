// src/router.js
import {createRouter, createWebHistory} from 'vue-router'
import {getLocalToken} from "@api/web/web.js";

const routes = [
    {
        path: '/',
        name: 'home',
        meta: {
            excludeInMenu: true,
            isRoot: true,
            title: '首页',
            desc: '首页',
            asSubParentTitle: '主页功能',
            icon: 'icon-home'
        },
        component: () => import('@views/HomeView.vue'),
        children: []
    },
    {
        path: '/settings',
        name: 'settings',
        component: () => import('@views/SettingsView.vue'),
        meta: {
            // excludeInMenu: true,
            group: '系统',
            isRoot: true,
            title: '设置',
            desc: '设置',
            asSubParentTitle: '设置',
            icon: 'Settings'
        },
    },
    {
        path: '/login',
        name: 'login',
        component: () => import('@views/Login.vue'),
        meta: {
            excludeInMenu: true,
            isRoot: true,
            title: '登录',
            desc: '登录',
            asSubParentTitle: '登录',
            icon: 'icon-home'
        },
    },
    {
        path: '/test/capabilities',
        name: 'capabilities',
        component: () => import('@views/CapabilitiesView.vue'),
        meta: {
            group: '演示',
            isRoot: true,
            title: '程序功能演示',
            desc: '程序功能演示',
            asSubParentTitle: '程序功能演示',
            // icon: 'icon-home'
        },
    }
    ,
    {
        path: '/Markdown',
        name: 'Markdown',
        component: () => import('@views/MarkdownView.vue'),
        meta: {
            group: '演示',
            isRoot: true,
            title: 'Markdown渲染',
            desc: 'Markdown渲染',
            asSubParentTitle: 'Markdown渲染',
            icon: 'Markdown'
        },
    }
    ,
    {
        path: '/AutoPlan/Config',
        name: 'AutoPlanConfig',
        component: () => import('@views/AutoPlanConfigView.vue'),
        meta: {
            group: 'JS扩展功能',
            isRoot: true,
            title: '自动体力计划配置',
            desc: '自动体力计划配置',
            asSubParentTitle: '自动体力计划配置',
            icon: 'Domain'
        },
    }
    ,
    {
        path: '/AutoPlan/Domain',
        name: 'AutoPlanDomain',
        component: () => import('@views/AutoPlanDomainView.vue'),
        meta: {
            group: 'JS扩展功能',
            isRoot: true,
            title: '全部秘境',
            desc: '全部秘境',
            asSubParentTitle: '全部秘境',
            icon: 'Domain'
        },
    }
    ,
    {
        name: 'SyntaxKey',
        path: '/SyntaxKey',
        component: () => import('@views/SyntaxKeyView.vue'),
        children: [
            // {
            //
            // }
        ],
        meta: {
            group: 'JS扩展功能',
            isRoot: true,
            title: '语法配置',
            desc: '语法配置',
            asSubParentTitle: '语法配置',
            icon: 'SyntaxKey'
        }
    },
    {
        name: 'Constants',
        path: '/Constants',
        component: () => import('@views/ConstantsView.vue'),
        children: [
            // {
            //
            // }
        ],
        meta: {
            group: 'JS扩展功能',
            isRoot: true,
            title: '常量配置',
            desc: '常量配置',
            asSubParentTitle: '常量配置',
            icon: 'SyntaxKey'
        }
    },
    {
        path: '/ws-proxy',
        name: 'WsProxy',
        component: () => import('@views/WsProxyView.vue'),
        meta: {
            group: 'JS扩展功能',
            isRoot: true,
            title: 'WebSocket代理授权管理',
            desc: 'WebSocket代理授权管理',
            asSubParentTitle: 'WebSocket代理授权管理',
            icon: 'Websocket-Proxy'
        }
    },
    {
        path: '/uid-manager',
        name: 'UidManager',
        component: () => import('@views/UidManagerView.vue'),
        meta: {
            isRoot: true,
            group: 'JS扩展功能',
            title: 'UID 映射管理',
            desc: 'UID 映射管理',
            asSubParentTitle: 'UID 映射管理',
            icon: 'UidMapping'
        }
    },
    {
        path: '/JavaScript/Api',
        name: 'JavaScriptApi',
        component: () => import('@views/JavaScriptApiView.vue'),
        meta: {
            isRoot: true,
            group: 'JS扩展功能',
            title: 'JavaScript API 文档',
            desc: '查看和管理脚本接口文档',
            asSubParentTitle: 'API 文档',
            icon: 'JavaScript'
        }
    },
    {
        path: '/logs',
        name: 'Logs',
        component: () => import('@views/LogView.vue'),
        meta: {
            // excludeInMenu: true,
            group: '系统',
            isRoot: true,
            title: '日志',
            desc: '日志',
            asSubParentTitle: '日志',
            icon: 'Logs'
        }
    },
    // 其他路由...
]
const VITE_BASE_PATH = (import.meta.env.VITE_BASE_PATH || '/bgi/ui/');
// console.log("VITE_BASE_PATH:", VITE_BASE_PATH);
const router = createRouter({
    history: createWebHistory(VITE_BASE_PATH),
    // history: createWebHistory("/bgi"),
    routes: routes,
})
router.beforeEach(async (to, from, next) => {
    // console.log('Navigating to:', to.path);
    //开发模式下，允许所有路由
    // if (import.meta.env.VITE_SERVER_PORT) {
    //     return next()
    // }
    let item = await getLocalToken()
    if (to.path === '/login') {
        if (item) {
            next('/')
        } else {
            next()
        }
    } else {
        if (item) {
            next()
        } else {
            next('/login')
        }

    }
})

export default router