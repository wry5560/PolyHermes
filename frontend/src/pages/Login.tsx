import { useState } from 'react'
import { useNavigate, Link } from 'react-router-dom'
import { Card, Form, Input, Button, message, Typography } from 'antd'
import { UserOutlined, LockOutlined } from '@ant-design/icons'
import { apiService } from '../services/api'
import { setToken } from '../utils'
import { useMediaQuery } from 'react-responsive'

const { Title } = Typography

const Login: React.FC = () => {
  const navigate = useNavigate()
  const isMobile = useMediaQuery({ maxWidth: 768 })
  const [loading, setLoading] = useState(false)
  const [form] = Form.useForm()

  const handleLogin = async (values: { username: string; password: string }) => {
    setLoading(true)
    try {
      const response = await apiService.auth.login(values)
      if (response.data.code === 0 && response.data.data) {
        const token = response.data.data.token
        setToken(token)
        message.success('登录成功')
        // 跳转到首页
        navigate('/')
      } else {
        message.error(response.data.msg || '登录失败')
      }
    } catch (error: any) {
      console.error('登录失败:', error)
      const errorMsg = error.response?.data?.msg || error.message || '登录失败'
      message.error(errorMsg)
    } finally {
      setLoading(false)
    }
  }

  return (
    <div style={{
      display: 'flex',
      justifyContent: 'center',
      alignItems: 'center',
      minHeight: '100vh',
      padding: isMobile ? '20px' : '40px',
      background: '#f0f2f5'
    }}>
      <Card
        style={{
          width: isMobile ? '100%' : '400px',
          boxShadow: '0 2px 8px rgba(0,0,0,0.1)'
        }}
      >
        <Title level={2} style={{ textAlign: 'center', marginBottom: '32px' }}>
          登录
        </Title>
        <Form
          form={form}
          onFinish={handleLogin}
          layout="vertical"
          size={isMobile ? 'large' : 'middle'}
        >
          <Form.Item
            name="username"
            rules={[
              { required: true, message: '请输入用户名' }
            ]}
          >
            <Input
              prefix={<UserOutlined />}
              placeholder="用户名"
              autoComplete="username"
            />
          </Form.Item>
          <Form.Item
            name="password"
            rules={[
              { required: true, message: '请输入密码' }
            ]}
          >
            <Input.Password
              prefix={<LockOutlined />}
              placeholder="密码"
              autoComplete="current-password"
            />
          </Form.Item>
          <Form.Item>
            <Button
              type="primary"
              htmlType="submit"
              block
              loading={loading}
              size={isMobile ? 'large' : 'middle'}
            >
              登录
            </Button>
          </Form.Item>
          <Form.Item style={{ marginBottom: 0, textAlign: 'right' }}>
            <Link to="/reset-password" style={{ fontSize: isMobile ? '14px' : '13px' }}>
              忘记密码？重置密码
            </Link>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}

export default Login

