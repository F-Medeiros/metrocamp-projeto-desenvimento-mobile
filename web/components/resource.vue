<template>
  <tr>
    <td class="text-right">
      <input type="checkbox">
    </td>
    <td class="text-muted text-right">{{number}}</td>
    <td class="text-left">
      <a href="#">
        <i class="{{icon}}"></i>
        {{name}}
      </a>
    </td>
    <td class="text-right">{{type}}</td>
    <td class="text-right">{{size}}</td>
    <td class="text-right">{{updated_at}}</td>
  </tr>
</template>

<script>
module.exports = {
  data: function() {
    return {
      msg: 'hello!'
    }
  }
}
</script>
