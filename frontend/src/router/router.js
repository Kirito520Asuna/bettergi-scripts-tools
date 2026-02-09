// src/router.js
import {createRouter, createWebHistory} from 'vue-router'

const routes = [
    {
        path: '/',
        name: 'home',
        meta: {
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
        path: '/AutoPlanDomainConfig',
        name: 'AutoPlanDomainConfig',
        component: () => import('@main/views/AutoPlanDomainConfigView'),
        meta: {
            isRoot: true,
            title: '自动秘境计划配置',
            desc: '自动秘境计划配置',
            asSubParentTitle: '自动秘境计划配置',
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

const router = createRouter({
    history: createWebHistory(import.meta.env.BASE_URL),
    routes: routes,
})
router.beforeEach((to, from, next) => {
    console.log('Navigating to:', to.path);
    next()
})

export default router