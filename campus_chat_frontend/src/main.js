import {
	createSSRApp
} from "vue";
import App from "./App.vue";
import uviewPlus from 'uview-plus'

export function createApp() {
	const app = createSSRApp(App);
	app.use(uviewPlus)
	
	// 使用本地字体文件，解决内网或离线环境图标不显示的问题
	// 必须在 app.use(uviewPlus) 之后设置，因为此时 uni.$u 才被挂载
	if (uni.$u && uni.$u.config) {
		uni.$u.config.iconUrl = '/static/uview-plus.ttf'
	}
	
	return {
		app,
	};
}
