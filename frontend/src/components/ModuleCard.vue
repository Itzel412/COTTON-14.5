<script setup>
const props = defineProps({
  title: {
    type: String,
    required: true,
  },
  description: {
    type: String,
    default: '',
  },
});
</script>

<template>
  <article class="module-card" @click="$emit('click')">
    <h3>{{ title }}</h3>
    <p>{{ description }}</p>
  </article>
</template>

<style scoped>
.module-card {
  background: #ffffff;
  padding: 1.2rem 1.1rem;
  border-radius: 16px;
  border: 1px solid var(--color-border, #dddddd);
  cursor: pointer;
  transition: transform 0.15s ease, box-shadow 0.15s ease,
    border-color 0.15s ease;
  max-width: 260px;
  margin: 0 auto;
  text-align: left;
}

.module-card h3 {
  margin-bottom: 0.4rem;
  font-size: 1.05rem;
  color: var(--cotton-dark, #1c262e);
}

.module-card p {
  font-size: 0.9rem;
  color: #555;
  line-height: 1.4;
}

.module-card:hover {
  transform: translateY(-3px);
  box-shadow: 0 8px 20px rgba(0, 0, 0, 0.08);
  border-color: var(--cotton-soft, #e9cba7);
}
</style>
