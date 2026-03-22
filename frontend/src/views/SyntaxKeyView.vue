<template>
  <div class="home">
    <div class="fixed-back">
      <button @click="goToBack" class="btn secondary">返回上一页</button>
    </div>
    <div class="fixed-footer">
      <button @click="goToHome" class="btn secondary">🏠 返回主页</button>
    </div>
  </div>
</template>
<script setup>
import {goBack, toHomePage} from "@api/web/web.js";

const goToHome = async () => {
  await toHomePage();
}
const goToBack = async () => {
  await goBack();
}
</script>
<style scoped>

</style>