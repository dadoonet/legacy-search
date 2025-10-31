import React, { useState, useEffect, useRef } from 'react'
import { Container, Row, Col, Form, Button, ProgressBar, Badge } from 'react-bootstrap'
import axios from 'axios'
import { Logger } from '../utils/logger'

interface Progress {
  took: number;
  rate: number;
  current: number;
}

const Init: React.FC = () => {
  const [persons, setPersons] = useState<string>('')
  const [status, setStatus] = useState<string>('')
  const [progress, setProgress] = useState<Progress>({
    took: 0,
    rate: 0,
    current: 0
  })
  const [remaining, setRemaining] = useState<number>(0)
  const [goal, setGoal] = useState<number>(0)
  const [took, setTook] = useState<number>(0)
  const [result, setResult] = useState<any>(null)
  
  const intervalRef = useRef<NodeJS.Timeout | null>(null)

  const startWatch = () => {
    if (intervalRef.current) return

    Logger.info('Started polling init status every 100ms')
    intervalRef.current = setInterval(async () => {
      try {
        const response = await axios.get('/api/1/person/_init_status')
        setProgress(response.data)
        
        // Calculate remaining docs
        const personsNum = parseInt(persons) || 0
        const remainingDocs = personsNum - response.data.current
        setRemaining(Math.round(remainingDocs / response.data.rate))
        setTook(Math.round(response.data.took / 1000))
        
        Logger.debug(`Init status: ${response.data.current}/${personsNum} (${response.data.rate} docs/sec, ~${Math.round(remainingDocs / response.data.rate)}s remaining)`)
      } catch (error) {
        Logger.error('Error polling status:', error)
      }
    }, 100)
  }

  const stopWatch = () => {
    if (intervalRef.current) {
      Logger.info('Stopped polling init status')
      clearInterval(intervalRef.current)
      intervalRef.current = null
    }
  }

  const handleInit = async () => {
    const personsNum = parseInt(persons) || 0
    Logger.info(`🚀 Starting initialization of ${personsNum} persons`)
    
    setStatus('')
    setResult(null)
    setProgress({
      took: 0,
      rate: 0,
      current: 0
    })
    setRemaining(0)
    setGoal(personsNum)
    setTook(0)
    
    startWatch()
    
    try {
      Logger.apiCall('GET', '/api/1/person/_init', { size: persons })
      const response = await axios.get(`/api/1/person/_init?size=${persons}`)
      
      Logger.apiResponse('GET', '/api/1/person/_init', response.data)
      Logger.info(`✅ Initialization completed successfully: ${response.data.current} persons processed`)
      
      setResult(response.data)
      setProgress(response.data)
      setStatus('bg-success')
      stopWatch()
    } catch (error) {
      Logger.apiError('GET', '/api/1/person/_init', error)
      Logger.error('❌ Initialization failed:', error)
      stopWatch()
    }
  }

  useEffect(() => {
    Logger.info('Init component mounted')
    return () => {
      Logger.info('Init component unmounting, cleaning up')
      stopWatch() // Cleanup on unmount
    }
  }, [])

  const progressPercentage = goal > 0 ? (progress.current / goal) * 100 : 0

  return (
    <Container fluid>
      <Row>
        <Col>
          <Form.Floating className="mb-3">
            <Form.Control
              id="nbBox"
              type="text"
              placeholder="Number of persons to inject"
              value={persons}
              onChange={(e) => setPersons(e.target.value)}
              autoComplete="off"
            />
            <label htmlFor="nbBox">Number of persons to inject</label>
          </Form.Floating>
        </Col>
        <Col>
          <Button variant="primary" size="lg" onClick={handleInit}>
            Inject{persons && parseInt(persons) > 0 && ` ${persons} persons`}
          </Button>
        </Col>
      </Row>

      <Row>
        <Col>
          <ProgressBar 
            now={progressPercentage} 
            style={{ height: '50px' }}
            className={status}
            striped
          >
            {progress.current > 0 && (
              <h2 style={{ margin: 0, lineHeight: '50px' }}>
                {progress.current} on {goal}
              </h2>
            )}
          </ProgressBar>
        </Col>
      </Row>

      <Row>
        <Col xs={3}>
          <h2>Elapsed</h2>
        </Col>
        <Col xs={9}>
          <h2>
            <Badge pill bg="primary">{took} s</Badge>
          </h2>
        </Col>
      </Row>

      <Row>
        <Col xs={3}>
          <h2>Estimated time left</h2>
        </Col>
        <Col xs={9}>
          <h2>
            <Badge pill bg="warning">{remaining} s</Badge>
          </h2>
        </Col>
      </Row>

      <Row>
        <Col xs={3}>
          <h2>Rate</h2>
        </Col>
        <Col xs={9}>
          <h2>
            <Badge pill bg="danger">{progress.rate} persons per sec</Badge>
          </h2>
        </Col>
      </Row>
    </Container>
  )
}

export default Init
