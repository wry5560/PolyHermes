import { useEffect, useState } from 'react'
import { useNavigate } from 'react-router-dom'
import { Card, Form, Button, Select, Switch, message, Typography, Space } from 'antd'
import { ArrowLeftOutlined, SaveOutlined } from '@ant-design/icons'
import { apiService } from '../services/api'
import { useAccountStore } from '../store/accountStore'
import type { Account, Leader, CopyTradingTemplate } from '../types'
import { useMediaQuery } from 'react-responsive'
import { formatUSDC } from '../utils'

const { Title } = Typography
const { Option } = Select

const CopyTradingAdd: React.FC = () => {
  const navigate = useNavigate()
  const isMobile = useMediaQuery({ maxWidth: 768 })
  const { accounts, fetchAccounts } = useAccountStore()
  const [form] = Form.useForm()
  const [loading, setLoading] = useState(false)
  const [leaders, setLeaders] = useState<Leader[]>([])
  const [templates, setTemplates] = useState<CopyTradingTemplate[]>([])
  
  useEffect(() => {
    fetchAccounts()
    fetchLeaders()
    fetchTemplates()
  }, [])
  
  const fetchLeaders = async () => {
    try {
      const response = await apiService.leaders.list({})
      if (response.data.code === 0 && response.data.data) {
        setLeaders(response.data.data.list || [])
      }
    } catch (error: any) {
      message.error(error.message || '获取 Leader 列表失败')
    }
  }
  
  const fetchTemplates = async () => {
    try {
      const response = await apiService.templates.list()
      if (response.data.code === 0 && response.data.data) {
        setTemplates(response.data.data.list || [])
      }
    } catch (error: any) {
      message.error(error.message || '获取模板列表失败')
    }
  }
  
  const handleSubmit = async (values: any) => {
    setLoading(true)
    try {
      const response = await apiService.copyTrading.create({
        accountId: values.accountId,
        templateId: values.templateId,
        leaderId: values.leaderId,
        enabled: values.enabled !== false
      })
      
      if (response.data.code === 0) {
        message.success('创建跟单成功')
        navigate('/copy-trading')
      } else {
        message.error(response.data.msg || '创建跟单失败')
      }
    } catch (error: any) {
      message.error(error.message || '创建跟单失败')
    } finally {
      setLoading(false)
    }
  }
  
  return (
    <div>
      <div style={{ marginBottom: 16 }}>
        <Button
          icon={<ArrowLeftOutlined />}
          onClick={() => navigate('/copy-trading')}
        >
          返回
        </Button>
      </div>
      
      <Card>
        <Title level={4}>新增跟单</Title>
        
        <Form
          form={form}
          layout="vertical"
          onFinish={handleSubmit}
          initialValues={{
            enabled: true
          }}
        >
          <Form.Item
            label="选择钱包"
            name="accountId"
            rules={[{ required: true, message: '请选择钱包' }]}
          >
            <Select placeholder="请选择钱包">
              {accounts.map(account => (
                <Option key={account.id} value={account.id}>
                  {account.accountName || `账户 ${account.id}`} ({account.walletAddress.slice(0, 6)}...{account.walletAddress.slice(-4)})
                </Option>
              ))}
            </Select>
          </Form.Item>
          
          <Form.Item
            label="选择模板"
            name="templateId"
            rules={[{ required: true, message: '请选择模板' }]}
          >
            <Select placeholder="请选择模板">
              {templates.map(template => (
                <Option key={template.id} value={template.id}>
                  {template.templateName} ({template.copyMode === 'RATIO' ? `比例 ${template.copyRatio}x` : `固定 ${template.fixedAmount ? formatUSDC(template.fixedAmount) : '0.0000'} USDC`})
                </Option>
              ))}
            </Select>
          </Form.Item>
          
          <Form.Item
            label="选择 Leader"
            name="leaderId"
            rules={[{ required: true, message: '请选择 Leader' }]}
          >
            <Select placeholder="请选择 Leader">
              {leaders.map(leader => (
                <Option key={leader.id} value={leader.id}>
                  {leader.leaderName || `Leader ${leader.id}`} ({leader.leaderAddress.slice(0, 6)}...{leader.leaderAddress.slice(-4)})
                </Option>
              ))}
            </Select>
          </Form.Item>
          
          <Form.Item
            label="启用状态"
            name="enabled"
            valuePropName="checked"
          >
            <Switch checkedChildren="开启" unCheckedChildren="停止" />
          </Form.Item>
          
          <Form.Item>
            <Space>
              <Button
                type="primary"
                htmlType="submit"
                icon={<SaveOutlined />}
                loading={loading}
              >
                创建跟单
              </Button>
              <Button onClick={() => navigate('/copy-trading')}>
                取消
              </Button>
            </Space>
          </Form.Item>
        </Form>
      </Card>
    </div>
  )
}

export default CopyTradingAdd

