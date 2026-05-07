import {getHostPrefix} from "@utils/ApiRequest.js";
class LogWebSocket {
    constructor() {
        this.ws = null
        this.reconnectTimer = null
        this.listeners = {}
        this.isConnected = false
        this.shouldReconnect = true
        this.retryCount = 0
        this.maxRetries = 10
    }

    connect(token, applicationId = null, filename = null, lines = '200') {
        this.lastToken = token
        this.lastApplicationId = applicationId
        this.lastFilename = filename
        this.lastLines = lines
        this.shouldReconnect = true
        this.retryCount = 0

        const basePath = getHostPrefix()
        let url = `${basePath.replace(/^\/|\/$/g, '')}/ws/logs?token=${token}`

        if (applicationId) {
            url += `&applicationId=${encodeURIComponent(applicationId)}`
        }

        if (filename) {
            url += `&filename=${encodeURIComponent(filename)}`
        }

        if (lines) {
            url += `&lines=${encodeURIComponent(lines)}`
        }

        const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
        const wsUrl = url.replace(/^https?:/, protocol)

        console.log('[LogWebSocket] 连接中...', wsUrl)

        this.ws = new WebSocket(wsUrl)

        this.ws.onopen = () => {
            console.log('[LogWebSocket] 连接成功')
            this.isConnected = true
            this.emit('connected', { message: '连接成功' })
        }

        this.ws.onmessage = (event) => {
            try {
                const data = JSON.parse(event.data)
                console.log('[LogWebSocket] 收到消息:', data)

                if (data.type === 'no-connected') {
                    console.log('[LogWebSocket] 实例未命中，准备重试...')
                    this.retryConnect()
                    return
                }

                this.emit(data.type, data)
            } catch (e) {
                console.error('[LogWebSocket] 解析消息失败:', e)
                this.emit('message', { type: 'raw', data: event.data })
            }
        }

        this.ws.onerror = (error) => {
            console.error('[LogWebSocket] 错误:', error)
            this.emit('error', { error })
        }

        this.ws.onclose = (event) => {
            console.log('[LogWebSocket] 连接关闭:', event.code, event.reason)
            this.isConnected = false
            this.emit('disconnected', { code: event.code, reason: event.reason })

            if (this.shouldReconnect && event.code !== 1000 && event.code !== 1001) {
                this.reconnect()
            }
        }
    }

    retryConnect() {
        if (this.retryCount >= this.maxRetries) {
            console.error('[LogWebSocket] 重试次数已达上限')
            this.emit('error', { message: '重试次数已达上限' })
            return
        }

        this.retryCount++
        console.log(`[LogWebSocket] 第 ${this.retryCount} 次重试连接...`)

        setTimeout(() => {
            if (this.shouldReconnect) {
                this.connect(this.lastToken, this.lastApplicationId, this.lastFilename, this.lastLines)
            }
        }, 2000)
    }

    reconnect() {
        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer)
        }

        this.reconnectTimer = setTimeout(() => {
            console.log('[LogWebSocket] 尝试重连...')
            this.connect(this.lastToken, this.lastApplicationId, this.lastFilename, this.lastLines)
        }, 3000)
    }

    disconnect() {
        this.shouldReconnect = false

        if (this.reconnectTimer) {
            clearTimeout(this.reconnectTimer)
            this.reconnectTimer = null
        }

        if (this.ws) {
            this.ws.close(1000, '主动断开')
            this.ws = null
        }

        this.isConnected = false
        this.listeners = {}
    }

    loadFile(applicationId, filename, lines = '200') {
        if (this.ws && this.isConnected) {
            this.lastApplicationId = applicationId
            this.lastFilename = filename
            this.lastLines = lines

            const message = {
                action: 'load_file',
                applicationId: applicationId,
                filename: filename,
                lines: lines
            }

            this.ws.send(JSON.stringify(message))
            console.log('[LogWebSocket] 请求加载文件:', applicationId, filename, lines)
        } else {
            console.error('[LogWebSocket] 未连接，无法加载文件')
        }
    }

    on(event, callback) {
        if (!this.listeners[event]) {
            this.listeners[event] = []
        }
        this.listeners[event].push(callback)
    }

    off(event, callback) {
        if (this.listeners[event]) {
            if (callback) {
                this.listeners[event] = this.listeners[event].filter(cb => cb !== callback)
            } else {
                delete this.listeners[event]
            }
        }
    }

    emit(event, data) {
        if (this.listeners[event]) {
            this.listeners[event].forEach(callback => {
                try {
                    callback(data)
                } catch (e) {
                    console.error(`[LogWebSocket] 事件回调执行失败 [${event}]:`, e)
                }
            })
        }
    }
}

export default new LogWebSocket()
