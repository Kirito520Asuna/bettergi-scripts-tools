import {ElMessage} from "element-plus";

const CopyToClipboard = async (text) => {

    try {
        // 强制将 text 转换为字符串
        const content = typeof text === 'object' ? JSON.stringify(text) : String(text);

        if (typeof navigator !== 'undefined' && navigator.clipboard) {
            // 现代浏览器使用 Clipboard API
            await navigator.clipboard.writeText(content || '');
        } else {
            // 降级处理：使用 execCommand
            const textarea = document.createElement('textarea');
            textarea.value = content || '';
            document.body.appendChild(textarea);
            textarea.select();
            document.execCommand('copy');
            document.body.removeChild(textarea);
        }
        /*alert('已复制到剪贴板！');*/
        ElMessage({
            type: 'success',
            message: `已复制到剪贴板！`,
        })
    } catch (err) {
        console.error('复制失败:', err);
        ElMessage({
            type: 'error',
            message: `复制失败，请手动复制内容。`,
        });
    }
};

export {
    CopyToClipboard
}