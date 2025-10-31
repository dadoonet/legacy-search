import React, { useState, useEffect } from 'react'
import { Container, Row, Col, Form, Table, Badge, Pagination } from 'react-bootstrap'
import { Link } from 'react-router-dom'
import axios from 'axios'
import { SearchResult, DateBucket } from '../types'
import { Logger } from '../utils/logger'

const Search: React.FC = () => {
  const [query, setQuery] = useState('')
  const [fDate, setFDate] = useState('')
  const [fCountry, setFCountry] = useState('')
  const [currentPage, setCurrentPage] = useState(1)
  const [totalItems, setTotalItems] = useState(0)
  const [result, setResult] = useState<SearchResult | null>(null)
  const [error, setError] = useState<string | null>(null)
  const [dates, setDates] = useState<DateBucket[]>([])

  const search = async (page: number, searchQuery?: string) => {
    try {
      setCurrentPage(page)
      const currentQuery = searchQuery !== undefined ? searchQuery : query
      const params = {
        size: 10,
        q: currentQuery,
        f_date: fDate,
        f_country: fCountry,
        from: (page - 1) * 10
      }
      
      Logger.apiCall('GET', '/api/1/person/_search', params)
      const response = await axios.get(`/api/1/person/_search`, { params })
      
      Logger.apiResponse('GET', '/api/1/person/_search', response.data, response.data.took)
      Logger.info(`Search completed: query="${currentQuery}", results=${response.data.hits.total.value}, took=${response.data.took}ms`)
      
      setError(null)
      setResult(response.data)
      setTotalItems(response.data.hits.total.value)

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
      Logger.error('Search failed:', err)
      setError('Backend not available')
    }
  }

  const addFilterCountry = (country: string) => {
    setFCountry(country)
    search(1)
  }

  const addFilterDate = (dateKey: string) => {
    setFDate(dateKey + '0')
    search(1)
  }

  const clearCountryFilter = () => {
    setFCountry('')
    search(1)
  }

  const clearDateFilter = () => {
    setFDate('')
    search(1)
  }

  const handlePageChange = (page: number) => {
    search(page)
  }

  useEffect(() => {
    Logger.info('Search component mounted, performing initial search')
    search(1)
  }, [])

  const handleQueryChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    const newQuery = e.target.value
    Logger.debug(`Query changed: "${query}" → "${newQuery}"${newQuery === '' ? ' (empty query)' : ''}`)
    setQuery(newQuery)
    search(1, newQuery)
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
              {fCountry.length > 0 && (
                <Badge bg="success" style={{ cursor: 'pointer', marginLeft: '5px' }} onClick={clearCountryFilter}>
                  {fCountry}
                </Badge>
              )}
              {fDate.length > 0 && (
                <Badge bg="info" style={{ cursor: 'pointer', marginLeft: '5px' }} onClick={clearDateFilter}>
                  {fDate}
                </Badge>
              )}
            </p>
          )}
          {error && (
            <p>
              <Badge bg="danger">{error}</Badge>
            </p>
          )}
        </Col>
      </Row>

      {result?.aggregations && (
        <Row>
          <Col md={3}>
            <Table striped bordered hover className="table-success table-condensed">
              <thead>
                <tr>
                  <th>Country</th>
                  <th>Count</th>
                </tr>
              </thead>
              <tbody>
                {result.aggregations['sterms#by_country'].buckets.map((bucket) => (
                  <tr key={bucket.key} onClick={() => addFilterCountry(bucket.key)} style={{ cursor: 'pointer' }}>
                    <td>{bucket.key}</td>
                    <td>{bucket.doc_count}</td>
                  </tr>
                ))}
              </tbody>
            </Table>
          </Col>
          <Col md={9}>
            <Table striped bordered hover className="table-info table-condensed">
              <thead>
                <tr>
                  {dates.map((bucket) => (
                    <th key={bucket.key} onClick={() => addFilterDate(bucket.key)} style={{ cursor: 'pointer' }}>
                      {bucket.key}0
                    </th>
                  ))}
                </tr>
              </thead>
              <tbody>
                <tr>
                  {dates.map((bucket) => (
                    <td key={bucket.key} onClick={() => addFilterDate(bucket.key)} style={{ cursor: 'pointer' }}>
                      {bucket.docs}
                    </td>
                  ))}
                </tr>
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

      <Row>
        <Col>
          <nav aria-label="Page navigation">
            <Pagination>
              {Array.from({ length: Math.ceil(totalItems / 10) }, (_, i) => (
                <Pagination.Item
                  key={i + 1}
                  active={i + 1 === currentPage}
                  onClick={() => handlePageChange(i + 1)}
                >
                  {i + 1}
                </Pagination.Item>
              ))}
            </Pagination>
          </nav>
        </Col>
      </Row>
    </Container>
  )
}

export default Search
