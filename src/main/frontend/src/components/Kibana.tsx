import React, { useEffect } from 'react'
import { Container, Row, Col, Card } from 'react-bootstrap'
import { Logger } from '../utils/logger'

const Kibana: React.FC = () => {
  useEffect(() => {
    Logger.info('Kibana component mounted')
  }, [])
  
  return (
    <Container fluid>
      <Row>
        <Col>
          <Card>
            <Card.Header>
              <h3>Kibana</h3>
            </Card.Header>
            <Card.Body>
              <a href="http://0.0.0.0:5601" target="_blank" rel="noopener noreferrer">
                Open Kibana
              </a>
            </Card.Body>
          </Card>
        </Col>

        <Col>
          <Card>
            <Card.Header>
              <h3>kibana.ndjson</h3>
            </Card.Header>
            <Card.Body>
              <a href="kibana/kibana.ndjson" target="_blank" rel="noopener noreferrer">
                kibana.ndjson
              </a>
              . You can import it from{' '}
              <a 
                href="http://0.0.0.0:5601/app/management/kibana/objects" 
                target="_blank" 
                rel="noopener noreferrer"
              >
                Saved Objects page
              </a>
              .
            </Card.Body>
          </Card>
        </Col>

        <Col>
          <Card>
            <Card.Header>
              <h3>console.txt</h3>
            </Card.Header>
            <Card.Body>
              <a href="kibana/console.txt" target="_blank" rel="noopener noreferrer">
                console.txt
              </a>
              . You can load it directly to the{' '}
              <a 
                href="http://0.0.0.0:5601/app/dev_tools#/console?load_from=https://raw.githubusercontent.com/dadoonet/legacy-search/00-legacy/src/main/resources/static/kibana/console.txt" 
                target="_blank" 
                rel="noopener noreferrer"
              >
                Dev Console
              </a>
              .
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </Container>
  )
}

export default Kibana
