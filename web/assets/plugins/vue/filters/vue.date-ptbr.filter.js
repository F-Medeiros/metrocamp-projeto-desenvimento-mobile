// usage: {{ date | datePtBr }}
Vue.filter('datePtBr', function (dateString) {

  return dateString.
          substring(0,10).
          split('-').
          reverse().
          join('/')
      + ' ' +
        dateString.
        substring(11,16);

});
