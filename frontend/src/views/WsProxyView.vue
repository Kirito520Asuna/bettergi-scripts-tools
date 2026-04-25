<template>
  <div class="home">
    <div class="container">
      <h1 class="title">Websocket代理授权管理</h1>

      <!-- 搜索栏 -->
      <div class="card search-layout">
        <div class="form-group search-layout">
          <label class="label">UID:</label>
          <input
            v-model="searchUid"
            class="input"
            placeholder="输入 UID 搜索"
            @keyup.enter="handleSearch"
          />
        </div>
        <div class="actions">
          <button @click="handleSearch" class="btn primary">🔍 搜索</button>
          <button @click="resetSearch" class="btn secondary">🔄 重置</button>
          <button @click="openAddDialog" class="btn success">➕ 新增</button>
          <button @click="batchDelete" class="btn danger" :disabled="selectedRows.length === 0">
            🗑️ 批量删除
          </button>
        </div>
      </div>

      <!-- 表格展示 -->
      <div class="card">
        <div class="table-container">
          <table class="data-table">
            <thead>
              <tr>
                <th>
                  <input
                    type="checkbox"
                    :checked="isAllSelected"
                    @change="toggleSelectAll"
                  />
                </th>
                <th>UID</th>
                <th>操作类型</th>
                <th>WS 地址</th>
                <th>代理地址</th>
                <th>Token</th>
                <th>AT 列表</th>
                <th>用户 ID</th>
                <th>群 ID</th>
                <th>操作</th>
              </tr>
            </thead>
            <tbody>
              <tr v-for="(item, index) in filteredData" :key="item.uid" class="data-row">
                <td>
                  <input
                    type="checkbox"
                    :value="item.uid"
                    v-model="selectedRows"
                  />
                </td>
                <td>{{ item.uid }}</td>
                <td>{{ actionMap.get(item.action)  || '-' }}</td>
                <td>{{ item.ws_url || '-' }}</td>
                <td>{{ item.ws_proxy_url || '-' }}</td>
                <td>
                  <span class="token-display">
                    {{ item.ws_token ? item.ws_token.substring(0, 8) + '***' : '-' }}
                  </span>
                </td>
                <td>{{ item.at_list|| '-' }}</td>
                <td>{{ item.user_id || '-' }}</td>
                <td>{{ item.group_id || '-' }}</td>
                <td>
                  <button @click="handleEdit(item)" class="btn-link edit">✏️ 编辑</button>
                  <button @click="handleDelete(item.uid)" class="btn-link danger">🗑️ 删除</button>
                </td>
              </tr>
            </tbody>
          </table>
          <div v-if="filteredData.length === 0" class="empty-data">
            暂无数据
          </div>
        </div>
      </div>

      <!-- 新增/编辑对话框 -->
      <div v-if="showDialog" class="modal-overlay" @click.self="closeDialog">
        <div class="modal-dialog">
          <div class="modal-header">
            <h3>{{ isEditMode ? '编辑授权' : '新增授权' }}</h3>
            <button @click="closeDialog" class="close-btn">✕</button>
          </div>
          <div class="modal-body">
            <div class="form-group">
              <label class="label">UID <span class="required">*</span></label>
              <input
                v-model="formData.uid"
                class="input"
                placeholder="请输入 UID"
                :disabled="isEditMode"
              />
            </div>
            <div class="form-group">
              <label class="label">操作类型<span class="required">*</span></label>
              <select v-model="formData.action" class="input">
                <option value="">请选择</option>
                <option value="send_private_msg">私聊</option>
                <option value="send_group_msg">群聊</option>
              </select>
            </div>
            <div class="form-group">
              <label class="label">WS 地址</label>
              <input
                v-model="formData.ws_url"
                class="input"
                placeholder="例如：ws://127.0.0.1:8080/ws"
              />
            </div>
            <div class="form-group">
              <label class="label">WS 代理地址</label>
              <input
                v-model="formData.ws_proxy_url"
                class="input"
                placeholder="可选，代理服务器地址"
              />
            </div>
            <div class="form-group">
              <label class="label">授权 Token</label>
              <input
                v-model="formData.ws_token"
                class="input"
                type="password"
                placeholder="可选，用于身份验证"
              />
            </div>
            <div class="form-group">
              <label class="label">AT 列表</label>
              <input
                v-model="formData.at_list"
                class="input"
                placeholder="可选，AT 相关人员列表"
              />
            </div>
            <div class="form-group">
              <label class="label">用户 ID</label>
              <input
                v-model="formData.user_id"
                class="input"
                placeholder="可选，QQ 用户 ID"
              />
            </div>
            <div class="form-group">
              <label class="label">群 ID</label>
              <input
                v-model="formData.group_id"
                class="input"
                placeholder="可选，QQ 群 ID"
              />
            </div>
          </div>
          <div class="modal-footer">
            <button @click="closeDialog" class="btn secondary">取消</button>
            <button @click="handleSubmit" class="btn primary">确定</button>
          </div>
        </div>
      </div>
    </div>
    <div class="fixed-back">
      <button @click="goToBack" class="btn secondary">返回上一页</button>
    </div>
    <div class="fixed-footer">
      <button @click="goToHome" class="btn secondary">🏠 返回主页</button>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { ElMessage } from 'element-plus';
import router from '@router/router';
import {goBack, toHomePage} from '@api/web/web.js';
import {
  getAccessAll,
  getAccess,
  saveAccess,
  deleteAccess
} from '@api/ws/wsProxy.js';
import {getHostPrefix} from "@utils/ApiRequest.js";
const actionMap=new Map([
    ['send_private_msg','私聊'],
    ['send_group_msg','群聊']
])
const currentRoute = router.currentRoute;
const tableData = ref([]);
const searchUid = ref('');
const selectedRows = ref([]);
const showDialog = ref(false);
const isEditMode = ref(false);
function getWsProxyHostPrefix(){
  return getHostPrefix();
}

const formData = ref({
  uid: '',
  action: '',
  ws_url: '',
  ws_proxy_url: getWsProxyHostPrefix()+'ws-proxy/message/send',
  ws_token: '',
  at_list: '',
  user_id: '',
  group_id: ''
});

const filteredData = computed(() => {
  if (!searchUid.value) return tableData.value;
  return tableData.value.filter(item =>
    item.uid.toLowerCase().includes(searchUid.value.toLowerCase())
  );
});

const isAllSelected = computed(() => {
  return tableData.value.length > 0 &&
         selectedRows.value.length === tableData.value.length;
});

onMounted(() => {
  fetchData();
});

const fetchData = async () => {
  try {
    const response = await getAccessAll();
    tableData.value = Array.isArray(response) ? response : [];
  } catch (error) {
    console.error('获取数据失败:', error);
    ElMessage.error('获取数据失败：' + (error.message || '未知错误'));
  }
};

const handleSearch = () => {
  // 使用 computed 属性自动过滤
};

const resetSearch = () => {
  searchUid.value = '';
};

const toggleSelectAll = () => {
  if (isAllSelected.value) {
    selectedRows.value = [];
  } else {
    selectedRows.value = tableData.value.map(item => item.uid);
  }
};

const openAddDialog = () => {
  isEditMode.value = false;
  formData.value = {
    uid: '',
    action: '',
    ws_url: '',
    ws_proxy_url: getWsProxyHostPrefix()+'ws-proxy/message/send',
    ws_token: '',
    at_list: '',
    user_id: '',
    group_id: ''
  };
  showDialog.value = true;
};

const handleEdit = (item) => {
  isEditMode.value = true;
  formData.value = { ...item };
  showDialog.value = true;
};

const closeDialog = () => {
  showDialog.value = false;
};

const handleSubmit = async () => {
  if (!formData.value.uid) {
    ElMessage.warning('请输入 UID');
    return;
  }else if (!formData.value.action) {
    ElMessage.warning('请输入操作类型');
    return;
  }

  try {
    await saveAccess(formData.value);
    ElMessage.success(isEditMode.value ? '更新成功' : '添加成功');
    closeDialog();
    await fetchData();
  } catch (error) {
    console.error('保存失败:', error);
    ElMessage.error('保存失败：' + (error.message || '未知错误'));
  }
};

const handleDelete = async (uid) => {
  try {
    await deleteAccess(uid);
    ElMessage.success('删除成功');
    await fetchData();
  } catch (error) {
    console.error('删除失败:', error);
    ElMessage.error('删除失败：' + (error.message || '未知错误'));
  }
};

const batchDelete = async () => {
  if (selectedRows.value.length === 0) {
    ElMessage.warning('请选择要删除的记录');
    return;
  }

  try {
    await deleteAccess(selectedRows.value.join(','));
    ElMessage.success(`成功删除 ${selectedRows.value.length} 条记录`);
    selectedRows.value = [];
    await fetchData();
  } catch (error) {
    console.error('批量删除失败:', error);
    ElMessage.error('批量删除失败：' + (error.message || '未知错误'));
  }
};

const goToHome = async () => {
  await toHomePage();
};
const goToBack = async () => {
  await goBack();
};
</script>

<style scoped>
.container {
  max-width: 1400px;
  height: 100vh;
  margin: 0 auto;
  padding: 20px;
/*  background: linear-gradient(135deg, #6a89cc 0%, #3498db 100%);*/
}

.title {
  color: white;
  font-size: 2rem;
  text-align: center;
  margin-bottom: 30px;
  text-shadow: 2px 2px 4px rgba(0,0,0,0.3);
}

.card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 20px;
  box-shadow: 0 4px 6px rgba(0,0,0,0.1);
}

.form-group {
  margin-bottom: 15px;
  display: flex;
}

.form-group.search-layout {
  display: flex;
  align-items: center;
  gap: 15px;
}
.label {
  width: 120px;
  flex-shrink: 0;
  font-weight: 600;
  color: #333;
}
.form-group.search-layout .label {
  width: auto;
  margin-bottom: 0;
  flex-shrink: 0;
}
.required {
  color: #ff4d4f;
  margin-left: 4px;
}

.input {
  flex: 1;
  padding: 10px;
  border: 2px solid #e0e0e0;
  border-radius: 6px;
  font-size: 14px;
  transition: all 0.3s;
}
.form-group.search-layout .input {
  flex: 1;
  min-width: 0;
}
.input:focus {
  border-color: #667eea;
  outline: none;
  box-shadow: 0 0 0 3px rgba(102, 126, 234, 0.1);
}

.actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-top: 15px;
}

.btn {
  padding: 10px 20px;
  border: none;
  border-radius: 6px;
  font-size: 14px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: 600;
}

.btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-primary {
  background: #667eea;
  color: white;
}

.btn-primary:hover {
  background: #5568d3;
}

.btn-secondary {
  background: #6c757d;
  color: white;
}

.btn-secondary:hover {
  background: #5a6268;
}

.btn-success {
  background: #28a745;
  color: white;
}

.btn-success:hover {
  background: #218838;
}

.btn-danger {
  background: #dc3545;
  color: white;
}

.btn-danger:hover {
  background: #c82333;
}

.table-container {
  height: 60vh;
  overflow-x: auto;
}

.data-table {
  width: 100%;
  border-collapse: collapse;
}

.data-table th,
.data-table td {
  padding: 12px;
  text-align: left;
  border-bottom: 1px solid #e0e0e0;
}

.data-table th {
  background: #f8f9fa;
  font-weight: 600;
  color: #333;
}

.data-row:hover {
  background: #f8f9fa;
}

.btn-link {
  background: none;
  border: none;
  cursor: pointer;
  padding: 4px 8px;
  font-size: 13px;
  margin-right: 5px;
  border-radius: 4px;
  transition: all 0.3s;
}

.btn-link.edit {
  color: #667eea;
}

.btn-link.edit:hover {
  background: #667eea;
  color: white;
}

.btn-link.danger {
  color: #dc3545;
}

.btn-link.danger:hover {
  background: #dc3545;
  color: white;
}

.empty-data {
  text-align: center;
  padding: 40px;
  color: #999;
}

.token-display {
  font-family: monospace;
  color: #666;
  background: #f5f5f5;
  padding: 2px 6px;
  border-radius: 3px;
}

.modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
}

.modal-dialog {
  background: white;
  border-radius: 12px;
  width: 90%;
  max-width: 600px;
  max-height: 90vh;
  overflow-y: auto;
  box-shadow: 0 10px 40px rgba(0,0,0,0.3);
}

.modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  border-bottom: 1px solid #e0e0e0;
}

.modal-header h3 {
  margin: 0;
  color: #333;
  font-size: 1.5rem;
}

.close-btn {
  background: none;
  border: none;
  font-size: 24px;
  cursor: pointer;
  color: #999;
  padding: 0;
  width: 30px;
  height: 30px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 50%;
  transition: all 0.3s;
}

.close-btn:hover {
  background: #f0f0f0;
  color: #333;
}

.modal-body {
  padding: 20px;
}

.modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 20px;
  border-top: 1px solid #e0e0e0;
}


@media (max-width: 768px) {
    .container {
        padding: 15px;
        max-width: 100%;
    }

    .title {
        font-size: 1.5rem;
        margin-bottom: 20px;
    }

    .card {
        padding: 15px;
        border-radius: 10px;
        margin-bottom: 15px;
    }

    .form-group {
        flex-direction: column;
        gap: 8px;
    }

    .label {
        width: auto;
        margin-bottom: 5px;
    }

    .input {
        width: auto;
    }

    .actions {
        flex-direction: column;
        gap: 8px;
    }

    .btn {
        width: 100%;
        padding: 12px 16px;
        font-size: 14px;
    }

    .table-container {
        height: 50vh;
        overflow-x: auto;
    }

    .data-table th,
    .data-table td {
        padding: 8px;
        font-size: 13px;
    }

    .btn-link {
        padding: 3px 6px;
        font-size: 12px;
        margin-right: 3px;
    }

    .empty-data {
        padding: 30px;
    }

    .modal-dialog {
        width: 95%;
        max-width: 90vw;
        max-height: 85vh;
    }

    .modal-header {
        padding: 15px;
    }

    .modal-body {
        padding: 15px;
    }

    .modal-footer {
        padding: 15px;
        flex-direction: column;
    }

    .modal-footer .btn {
        width: auto;
    }
}

@media (max-width: 480px) {
    .container {
        padding: 10px;
    }

    .title {
        font-size: 1.2rem;
        margin-bottom: 15px;
    }

    .card {
        padding: 12px;
        border-radius: 8px;
        margin-bottom: 12px;
    }

    .form-group.search-layout {
        flex-direction: column;
        align-items: stretch;
        gap: 8px;
    }

    .label {
        font-size: 13px;
    }

    .input {
        font-size: 13px;
        padding: 8px;
    }

    .actions {
        gap: 6px;
    }

    .btn {
        padding: 10px 14px;
        font-size: 13px;
    }

    .table-container {
        height: 45vh;
    }

    .data-table th,
    .data-table td {
        padding: 6px;
        font-size: 12px;
    }

    .btn-link {
        padding: 2px 5px;
        font-size: 11px;
        margin-right: 2px;
    }

    .empty-data {
        padding: 20px;
        font-size: 13px;
    }

    .token-display {
        font-size: 11px;
        padding: 2px 4px;
    }

    .modal-overlay {
        align-items: flex-end;
    }

    .modal-dialog {
        width: 100%;
        max-width: 100vw;
        max-height: 95vh;
        border-radius: 12px 12px 0 0;
    }

    .modal-header h3 {
        font-size: 1.2rem;
    }

    .close-btn {
        width: 28px;
        height: 28px;
        font-size: 22px;
    }

    .modal-body {
        padding: 12px;
    }

    .modal-footer {
        padding: 12px;
        gap: 8px;
    }
}

</style>
