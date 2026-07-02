import service from "@utils/request.js";

export async function getLogAuthToken() {
    const response = await service.get('/jwt/log/auth-token');
    return response
}

export async function getFileNames(applicationId) {
    return await service.get('/jwt/log/file-names', {
        params: {applicationId: applicationId}
    })
}


export async function analysisBgiLog(fileName, byte, targetTimestamp) {

    const {code, data} = await service.post(
        '/jwt/log/analysis/bgi-log'
        , {
            fileName: fileName,
            data: [...byte],
            targetTimestamp: targetTimestamp
        }
    )
    return data
}

export class BgiLog {
    /**
     * 主入口：分析日志
     * @param {string} fileName
     * @param {Uint8Array|ArrayBuffer} data - 字节数据（前端 FileReader 可读出 ArrayBuffer）
     * @param {string} targetTimestamp - 目标时间戳（例如 "18:20:24.335"）
     * @returns {BgiLog}
     */
    static analysisBgiLog(fileName, data, targetTimestamp) {

        // 确保 targetTimestamp 格式为 [HH:mm:ss.SSS]
        if (targetTimestamp && targetTimestamp.trim().length > 0) {
            if (!targetTimestamp.startsWith('[')) {
                targetTimestamp = '[' + targetTimestamp;
            }
            if (!targetTimestamp.endsWith(']')) {
                targetTimestamp = targetTimestamp + ']';
            }
        } else {
            targetTimestamp = null; // 标准化为空
        }
        console.log('[BGI-LOG] targetTimestamp:', targetTimestamp || 'ALL')
        const list = this.analysisLine(data, targetTimestamp) || []
        let lastTimestamp = null;

        if (list.length > 0) {
            const last = list[list.length - 1];
            const match = last.match(/\[\d{2}:\d{2}:\d{2}\.\d{3}\]/);
            if (match) {
                lastTimestamp = match[0];
            }
        }
        const timestamp = lastTimestamp || targetTimestamp
        return {
            fileName: fileName,
            logLines: list,
            timestamp: timestamp
        };
    }

    /**
     * 解析字节数据，得到合并并过滤后的日志行
     * @param {Uint8Array|ArrayBuffer} data
     * @param {string|null} targetTimestamp
     * @returns {string[]}
     */
    static analysisLine(data, targetTimestamp) {
        let lines = [];

        // 1. 将字节数据解码为 UTF-8 字符串，再按行分割
        if (data && (data instanceof Uint8Array || data instanceof ArrayBuffer)) {
            const decoder = new TextDecoder('utf-8');
            const uint8 = data instanceof ArrayBuffer ? new Uint8Array(data) : data;
            const content = decoder.decode(uint8);
            lines = content.split(/\r?\n/); // 兼容 CRLF 和 LF 换行
        }

        // 2. 合并多行日志（堆栈等）
        lines = this.mergeLogLines(lines) || []

        // 3. 从目标时间戳截取
        lines = this.extractLinesAfterTimestamp(lines, targetTimestamp) || []

        // 4. 如果指定了目标时间戳，则去掉该时间戳所在的第一行（原 Java 逻辑：排除匹配行开头的那一条）
        if (targetTimestamp && targetTimestamp.trim().length > 0) {
            lines = lines.filter(line => !line.startsWith(targetTimestamp));
        }

        return lines || []
    }

    /**
     * 合并日志行：将以 [HH:mm:ss.SSS] 开头的行视为新行开头，其余行作为上一行的续行拼接
     * @param {string[]} allLines
     * @returns {string[]}
     */
    static mergeLogLines(allLines) {
        if (!allLines || allLines.length === 0) return [];

        const merged = [];
        let current = '';

        const timeRegex = /^\[\d{2}:\d{2}:\d{2}\.\d{3}\]/;

        for (const line of allLines) {
            if (timeRegex.test(line)) {
                if (current.length > 0) {
                    merged.push(current);
                }
                current = line;
            } else {
                if (current.length > 0) {
                    current += '\n' + line;
                } else {
                    current = line; // 非常少见：第一行不是时间戳开头
                }
            }
        }

        if (current.length > 0) {
            merged.push(current);
        }

        return merged;
    }

    /**
     * 提取指定时间戳之后的日志行（包含该时间戳所在行）
     * @param {string[]} allLines - 建议传入已合并的行
     * @param {string|null} targetTimestamp - 如 "[18:20:24.335]"
     * @returns {string[]}
     */
    static extractLinesAfterTimestamp(allLines = [], targetTimestamp) {
        if (!targetTimestamp || targetTimestamp.trim().length === 0) {
            return allLines;
        }

        // 保证格式统一为 [HH:mm:ss.SSS]
        let ts = targetTimestamp.trim();
        if (!ts.startsWith('[')) ts = '[' + ts;
        if (!ts.endsWith(']')) ts = ts + ']';

        const startIndex = allLines.findIndex(line => line.includes(ts));

        if (startIndex === -1) {
            console.warn(`[BGI-LOG] 未找到目标时间戳: ${ts}`);
            return [];
        }

        // 从 startIndex 开始截取到末尾（包含该行）
        return allLines.slice(startIndex);
    }
}