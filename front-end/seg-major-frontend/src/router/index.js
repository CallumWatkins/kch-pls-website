import Vue from 'vue'
import VueRouter from 'vue-router'
import VueMeta from 'vue-meta'

import Home from '../views/Home.vue'
import News from '../views/News.vue'

import AdminLayout from '../views/AdminLayout.vue'
import AdminHome from '../views/AdminHome.vue'
import AdminDrugChart from '../views/AdminDrugChart.vue'
import AdminAllSites from '../views/AdminAllSites.vue'
import AdminNewSite from '../views/AdminNewSite.vue'
import AdminSiteLayout from '../views/AdminSiteLayout.vue'
import AdminAllPages from '../views/AdminAllPages.vue'
import AdminEditPage from '../views/AdminEditPage.vue'
import SiteContentViewerLayout from '../views/SiteContentViewerLayout.vue'
import PageViewer from '../views/PageViewer.vue'
import NewsViewer from '../views/NewsViewer.vue'
import AdminSiteSettings from '../views/AdminSiteSettings.vue'
import SearchResults from '../views/SearchResults.vue'
import Login from '../views/Login.vue'
import DrugChartCreator from '../views/DrugChartCreator.vue'
import AllPages from '../views/AllPages.vue'
import PageEmptyState from '../views/PageEmptyState.vue'

import UserService from "@/services/user-service";


Vue.use(VueRouter)
Vue.use(VueMeta)

const routes = [
  {
    path: '/',
    name: 'Home',
    component: Home
  },
  {
    path: '/news',
    component: News,
    props(route) {
      if (route.query.page) return { currentPageNumber: /^\d+$/.test(route.query.page) ? +route.query.page : undefined }
    }
  },
  {
    path: '/news/:newsSlug',
    component: NewsViewer
  },
  {
    path: '/drug-chart',
    component: DrugChartCreator
  },
  {
    path: '/admin',
    component: AdminLayout,
    children: [
      {
        path: '',
        component: AdminHome
      },   
      {
        path: 'drug-chart',
        component: AdminDrugChart,
      },
      {
        path: 'sites',
        component: AdminAllSites,
      },
      {
        path: 'sites/new',
        component: AdminNewSite
      },
      {
        path: 'sites/:siteSlug',
        component: AdminSiteLayout,
        children: [
          {
            path: '',
            redirect: 'pages'
          },
          {
            path: 'settings',
            component: AdminSiteSettings
          },
          {
            path: 'pages',
            component: AdminAllPages
          },
          {
            path: 'pages/new',
            component: AdminEditPage,
            props: { newPage: true }
          },
          {
            path: 'pages/:pageSlug',
            component: AdminEditPage,
            props: { newPage: false }
          }
        ]
      }
    ]
  },
  {
    path: '/login',
    component: Login,
    props: route => ({ sessionExpired: route.query.exp == "true" })
  },
  {
    path: '/:siteSlug',
    component: SiteContentViewerLayout,
    children: [
      {
        path: 'search',
        component: SearchResults
      },
      {
        path: 'all-pages',
        component: AllPages
      },
      {
        path: '',
        component: PageEmptyState
      },
      {
        path: ':pageSlug',
        component: PageViewer
      }
    ]
  }
]

const router = new VueRouter({
  mode: 'history',
  base: process.env.BASE_URL,
  routes
})

// Authentication Navigation Guard 
router.beforeEach((to, from, next) => {
  if (to.path.startsWith('/admin') && !UserService.isAuthenticated()) next('/login')
  else next()
})

export default router
