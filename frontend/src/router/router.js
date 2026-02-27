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
        component: () => import('@main/views/HomeView'),
        children: []
    },
    {
        path: '/settings',
        name: 'settings',
        component: () => import('@main/views/SettingsView'),
        meta: {
            excludeInMenu: true,
            isRoot: true,
            title: '设置',
            desc: '设置',
            asSubParentTitle: '设置',
            icon: 'icon-home'
        },
    },
    {
        path: '/login',
        name: 'login',
        component: () => import('@main/views/Login'),
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
        path: '/capabilities',
        name: 'capabilities',
        component: () => import('@main/views/CapabilitiesView'),
        meta: {
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
        component: () => import('@main/views/MarkdownView'),
        meta: {
            isRoot: true,
            title: 'Markdown渲染',
            desc: 'Markdown渲染',
            asSubParentTitle: 'Markdown渲染',
            icon: 'Markdown'
        },
    }
    ,
    {
        path: '/AutoPlanConfig',
        name: 'AutoPlanConfig',
        component: () => import('@views/AutoPlanConfigView.vue'),
        meta: {
            isRoot: true,
            title: '自动体力计划配置',
            desc: '自动体力计划配置',
            asSubParentTitle: '自动体力计划配置',
            icon: 'Domain'
        },
    }
    ,
    {
        path: '/AutoPlanDomain',
        name: 'AutoPlanDomain',
        component: () => import('@main/views/AutoPlanDomainView'),
        meta: {
            isRoot: true,
            title: '全部秘境展示',
            desc: '全部秘境展示',
            asSubParentTitle: '全部秘境展示',
            icon: 'Domain'
        },
    }
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
    if (import.meta.env.VITE_SERVER_PORT) {
        return next()
    }
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