'use strict';

// Register `search` component, along with its associated controller and template
angular.
  module('search').
  component('search', {
    templateUrl: 'search/search.template.html',
    controller: ['$http', function SearchController($http) {
      var self = this;
      self.query = "";
      self.f_date = "";
      self.f_country = "";
      self.currentPage = 1;
      self.totalItems = 0;

      self.search = function(page) {
        self.currentPage = page;
        $http({method: 'GET', url: '/api/1/person/_search?size=10&q='+ self.query
        + '&f_date=' + self.f_date + '&f_country=' + self.f_country + '&from=' + (page-1)*10 })
            .success(function(data) {
              self.error = null;
              self.result = data;
              self.totalItems = data.hits.total;
              // Group data every 10 years (facets don't support it yet)
              self.dates = new Array();

              // If we have aggs, compute (for future use)
              if (data.aggregations) {
                var buckets = data.aggregations['date_histogram#by_year'].buckets;

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
            })
            .error(function(data, status, headers, config) {
              self.error = "Backend not available";
            });
      };

      self.addFilterCountry = function(bucket) {
        console.log(bucket.key);
        self.f_country = bucket.key;
        self.search(1);
      };

      self.addFilterDate = function(bucket) {
        console.log(bucket.key+"0");
        self.f_date = bucket.key+"0";
        self.search(1);
      };

      self.changePage = function() {
        self.search(self.currentPage);
      };

      self.search(1);
    }]
  });
