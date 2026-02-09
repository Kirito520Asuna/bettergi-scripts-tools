import {ElMessage} from "element-plus";

const CopyToClipboard = (text) => {

    try {
        navigator.clipboard.writeText(text || '');
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