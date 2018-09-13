'use strict';

// Register `compute` component, along with its associated controller and template
angular.
  module('compute').
  component('compute', {
    templateUrl: 'compute/compute.template.html',
    controller: ['$http', function ComputeController($http) {
      var self = this;
      self.query = "";
      self.f_date = "";
      self.f_country = "";

      self.search = function() {
        $http({method: 'GET', url: '/api/1/person/_search?from=0&size=10&q='+ self.query
        + '&f_date=' + self.f_date + '&f_country=' + self.f_country })
            .then(function successCallback(response) {
              self.error = null;
              self.result = response.data;

              // Group data every 10 years (facets don't support it yet)
              self.dates = new Array();

              // If we have aggs, compute (for future use)
              if (self.result.aggregations) {
                  var buckets = self.result.aggregations['date_histogram#by_year'].buckets;

                var i = -1;
                for (var idx in buckets) {
                  var year = buckets[idx].key_as_string;
                  var docs = buckets[idx].doc_count;
                  var subyear = year.substr(0,3);

                  if (i == -1 || subyear != self.dates[i].key) {
                    i++;
                    self.dates[i] = {};
                    self.dates[i].key = subyear;
                    self.dates[i].docs = docs;
                  } else {
                    self.dates[i].docs += docs;
                  }
                }
              }
            }, function errorCallback(response) {
                self.error = "Backend not available";
            });
      };

      self.addFilterCountry = function(bucket) {
        console.log(bucket.key);
        self.f_country = bucket.key;
        self.search();
      };

      self.addFilterDate = function(bucket) {
        console.log(bucket.key+"0");
        self.f_date = bucket.key+"0";
        self.search();
      };

      self.search();
    }]
  });
