// Simple logger for frontend development
export class Logger {
  private static prefix = '[LegacySearch]'
  
  static info(message: string, ...args: any[]) {
    console.log(`${this.prefix} ℹ️`, message, ...args)
  }
  
  static warn(message: string, ...args: any[]) {
    console.warn(`${this.prefix} ⚠️`, message, ...args)
  }
  
  static error(message: string, ...args: any[]) {
    console.error(`${this.prefix} ❌`, message, ...args)
  }
  
  static debug(message: string, ...args: any[]) {
    console.debug(`${this.prefix} 🐛`, message, ...args)
  }
  
  static apiCall(method: string, url: string, params?: any) {
    console.log(`${this.prefix} 🌐 API ${method.toUpperCase()}`, url, params ? { params } : '')
  }
  
  static apiResponse(method: string, url: string, data: any, took?: number) {
    console.log(`${this.prefix} ✅ API Response ${method.toUpperCase()}`, url, {
      dataSize: JSON.stringify(data).length,
      took: took ? `${took}ms` : 'unknown',
      data: data
    })
  }
  
  static apiError(method: string, url: string, error: any) {
    console.error(`${this.prefix} ❌ API Error ${method.toUpperCase()}`, url, error)
  }
}
