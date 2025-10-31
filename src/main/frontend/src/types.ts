export interface Person {
  _id: string;
  _source: {
    name: string;
    gender: string;
    dateOfBirth: string;
    address: {
      country: string;
      city: string;
    };
  };
  _score?: number;
}

export interface SearchResult {
  hits: {
    total: {
      value: number;
    };
    hits: Person[];
  };
  took: number;
  aggregations?: {
    'sterms#by_country': {
      buckets: Array<{
        key: string;
        doc_count: number;
      }>;
    };
    'date_histogram#by_year': {
      buckets: Array<{
        key_as_string: string;
        doc_count: number;
      }>;
    };
  };
}

export interface DateBucket {
  key: string;
  docs: number;
}
