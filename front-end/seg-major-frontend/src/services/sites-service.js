import BackendService from './backend-service'

export default {
    async createSite(siteName) {
        const res = await BackendService.createSite(siteName)
        return res.data
    },

    async getAllSites() {
        const res = await BackendService.getAllSites()
        return res.data
    }
}