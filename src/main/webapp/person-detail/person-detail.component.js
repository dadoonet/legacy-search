'use strict';

// Register `personDetail` component, along with its associated controller and template
angular.
  module('personDetail').
  component('personDetail', {
    templateUrl: 'person-detail/person-detail.template.html',
    controller: ['$http', '$routeParams',
      function PersonDetailController($http, $routeParams) {
          var self = this;

          $http.get('/api/1/person/_byid/'+ $routeParams.id).then(function(response) {
              console.log(response.data);
              self.person = response.data;
          });

          self.save = function() {
              $http.put('/api/1/person/'+ $routeParams.id , self.person)
                  .then(function() { console.log( self.person ); });
          };

          self.delete = function() {
              $http.delete('/api/1/person/'+ $routeParams.id);
              $location.path('/');
          };
      }
    ]
  });
