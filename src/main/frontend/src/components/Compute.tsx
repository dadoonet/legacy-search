import React, { useState, useEffect } from 'react'
import { Container, Row, Col, Form, Table, Badge } from 'react-bootstrap'
import { Link } from 'react-router-dom'
import axios from 'axios'
import { SearchResult, DateBucket } from '../types'
import { Logger } from '../utils/logger'

interface ComputeSearchResult extends SearchResult {
  aggregations?: {
    'sterms#by_country': {
      buckets: Array<{
        key: string;
        doc_count: number;
        'date_histogram#by_year': {
          buckets: Array<{
            key_as_string: string;
            doc_count: number;
            'avg#avg_children': {
              value: number;
            };
          }>;
        };
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

const Compute: React.FC = () => {
  const [query, setQuery] = useState('')
  const [fDate, setFDate] = useState('')
  const [fCountry, setFCountry] = useState('')
  const [result, setResult] = useState<ComputeSearchResult | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [dates, setDates] = useState<DateBucket[]>([])

  const search = async () => {
    try {
      const params = {
        from: 0,
        size: 10,
        q: query,
        f_date: fDate,
        f_country: fCountry
      }
      
      Logger.apiCall('GET', '/api/1/person/_search', params)
      const response = await axios.get('/api/1/person/_search', { params })
      
      Logger.apiResponse('GET', '/api/1/person/_search', response.data, response.data.took)
      Logger.info(`Compute search completed: query="${query}", results=${response.data.hits.total.value}, took=${response.data.took}ms`)
      
      setError(null)
      setResult(response.data)

      // Group data every 10 years (facets don't support it yet)
      const newDates: DateBucket[] = []
      
      if (response.data.aggregations) {
        const buckets = response.data.aggregations['date_histogram#by_year'].buckets
        
        let i = -1
        for (const bucket of buckets) {
          const year = bucket.key_as_string
          const docs = bucket.doc_count
          const subyear = year.substr(0, 3)
          
          if (i === -1 || subyear !== newDates[i].key) {
            i++
            newDates[i] = {
              key: subyear,
              docs: docs
            }
          } else {
            newDates[i].docs += docs
          }
        }
      }
      setDates(newDates)
      
    } catch (err) {
      Logger.apiError('GET', '/api/1/person/_search', err)
      Logger.error('Compute search failed:', err)
      setError('Backend not available')
    }
  }

  const addFilterCountry = (country: string) => {
    setFCountry(country)
    // search() will be called by useEffect
  }

  const addFilterDate = (dateKey: string) => {
    setFDate(dateKey + '0')
    // search() will be called by useEffect
  }

  useEffect(() => {
    Logger.info('Compute component mounted, performing initial search')
    search()
  }, [])
  
  useEffect(() => {
    Logger.debug(`Compute search triggered by dependency change: query="${query}", fDate="${fDate}", fCountry="${fCountry}"`)
    search()
  }, [query, fDate, fCountry])

  const handleQueryChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newQuery = e.target.value
    Logger.debug(`Compute query changed: "${query}" → "${newQuery}"${newQuery === '' ? ' (empty query)' : ''}`)
    setQuery(newQuery)
  }

  return (
    <Container fluid>
      <Row>
        <Col>
          <Form.Floating className="mb-3">
            <Form.Control
              id="searchBox"
              type="text"
              placeholder="Type something..."
              value={query}
              onChange={handleQueryChange}
              autoComplete="off"
            />
            <label htmlFor="searchBox">Type something...</label>
          </Form.Floating>
        </Col>
      </Row>

      <Row>
        <Col>
          {!error && result && (
            <p>
              Found <Badge bg="primary">{result.hits.total.value}</Badge> hits in{' '}
              <Badge bg="primary">{result.took} ms</Badge>
            </p>
          )}
          {error && (
            <p>
              <Badge bg="danger">{error}</Badge>
            </p>
          )}
        </Col>
      </Row>

      {result?.aggregations?.['sterms#by_country'] && (
        <Row>
          <Col md={12}>
            <Table striped bordered hover className="table-success table-condensed">
              <thead>
                <tr>
                  <th>Country</th>
                  <th>Count</th>
                  <th>Per Year</th>
                </tr>
              </thead>
              <tbody>
                {result.aggregations['sterms#by_country'].buckets.map((bucket) => (
                  <tr key={bucket.key}>
                    <td>{bucket.key}</td>
                    <td>{bucket.doc_count}</td>
                    <td>
                      {bucket['date_histogram#by_year']?.buckets && bucket['date_histogram#by_year'].buckets.length > 0 && (
                        <Table striped bordered hover className="table-info table-condensed">
                          <thead>
                            <tr>
                              <th>year</th>
                              {bucket['date_histogram#by_year'].buckets.map((year) => (
                                <th key={year.key_as_string}>{year.key_as_string}</th>
                              ))}
                            </tr>
                          </thead>
                          <tbody>
                            <tr>
                              <td>persons</td>
                              {bucket['date_histogram#by_year'].buckets.map((year) => (
                                <td key={year.key_as_string}>{year.doc_count}</td>
                              ))}
                            </tr>
                            <tr className="table-danger">
                              <td>children</td>
                              {bucket['date_histogram#by_year'].buckets.map((year) => (
                                <td key={year.key_as_string}>
                                  {year['avg#avg_children']?.value ? year['avg#avg_children'].value.toFixed(1) : 'N/A'}
                                </td>
                              ))}
                            </tr>
                          </tbody>
                        </Table>
                      )}
                    </td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </Col>
        </Row>
      )}

      <Row>
        <Col md={12}>
          <Table striped bordered hover className="table-condensed">
            <thead>
              <tr>
                <th>Name</th>
                <th>Gender</th>
                <th>Date Of Birth</th>
                <th>Country</th>
                <th>City</th>
                <th>Score</th>
              </tr>
            </thead>
            <tbody>
              {result?.hits.hits.map((entry) => (
                <tr key={entry._id}>
                  <td>
                    <Link to={`/person/${entry._id}`}>{entry._source.name}</Link>
                  </td>
                  <td>{entry._source.gender}</td>
                  <td>{entry._source.dateOfBirth}</td>
                  <td>{entry._source.address.country}</td>
                  <td>{entry._source.address.city}</td>
                  <td>
                    {entry._score && entry._score !== 1 && (
                      <span>{entry._score}</span>
                    )}
                  </td>
                </tr>
              ))}
            </tbody>
          </Table>
        </Col>
      </Row>
    </Container>
  )
}

export default Compute
