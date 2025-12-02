import { useState, useEffect } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { Layout as AntLayout, Menu, Drawer, Button, Modal } from 'antd'
import { useMediaQuery } from 'react-responsive'
import {
  WalletOutlined,
  UserOutlined,
  UnorderedListOutlined,
  BarChartOutlined,
  MenuOutlined,
  FileTextOutlined,
  LinkOutlined,
  AppstoreOutlined,
  TeamOutlined,
  LogoutOutlined,
  SettingOutlined
} from '@ant-design/icons'
import type { MenuProps } from 'antd'
import type { ReactNode } from 'react'
import { removeToken } from '../utils'
import { wsManager } from '../services/websocket'

const { Header, Content, Sider } = AntLayout

interface LayoutProps {
  children: ReactNode
}

const Layout: React.FC<LayoutProps> = ({ children }) => {
  const navigate = useNavigate()
  const location = useLocation()
  const isMobile = useMediaQuery({ maxWidth: 768 })
  const [mobileMenuOpen, setMobileMenuOpen] = useState(false)
  
  // 获取当前选中的菜单项
  const getSelectedKeys = (): string[] => {
    return [location.pathname]
  }
  
  // 获取当前应该打开的父菜单
  const getInitialOpenKeys = (): string[] => {
    const path = location.pathname
    const keys: string[] = []
    if (path.startsWith('/leaders') || path.startsWith('/templates') || path.startsWith('/copy-trading')) {
      keys.push('/copy-trading-management')
    }
    return keys
  }
  
  const [openKeys, setOpenKeys] = useState<string[]>(getInitialOpenKeys())
  
  // 当路径变化时，自动打开对应的父菜单
  useEffect(() => {
    const path = location.pathname
    const keys: string[] = []
    if (path.startsWith('/leaders') || path.startsWith('/templates') || path.startsWith('/copy-trading')) {
      keys.push('/copy-trading-management')
    }
    setOpenKeys(keys)
  }, [location.pathname])
  
  const menuItems: MenuProps['items'] = [
    {
      key: '/accounts',
      icon: <WalletOutlined />,
      label: '账户管理'
    },
    {
      key: '/copy-trading-management',
      icon: <AppstoreOutlined />,
      label: '跟单交易',
      children: [
        {
          key: '/leaders',
          icon: <UserOutlined />,
          label: 'Leader 管理'
        },
        {
          key: '/templates',
          icon: <FileTextOutlined />,
          label: '跟单模板'
        },
        {
          key: '/copy-trading',
          icon: <LinkOutlined />,
          label: '跟单配置'
        }
      ]
    },
    {
      key: '/positions',
      icon: <UnorderedListOutlined />,
      label: '仓位管理'
    },
    {
      key: '/statistics',
      icon: <BarChartOutlined />,
      label: '统计信息'
    },
    {
      key: '/users',
      icon: <TeamOutlined />,
      label: '用户管理'
    },
    {
      key: '/system-settings',
      icon: <SettingOutlined />,
      label: '系统管理'
    },
    {
      key: 'logout',
      icon: <LogoutOutlined />,
      label: '退出登录'
    }
  ]
  
  const handleLogout = () => {
    removeToken()
    // 断开 WebSocket 连接
    wsManager.disconnect()
    navigate('/login', { replace: true })
  }
  
  const handleLogoutConfirm = () => {
    Modal.confirm({
      title: '确认退出',
      content: '确定要退出登录吗？',
      okText: '确定',
      cancelText: '取消',
      onOk: () => {
        handleLogout()
        if (isMobile) {
          setMobileMenuOpen(false)
        }
      }
    })
  }
  
  const handleMenuClick = ({ key }: { key: string }) => {
    // 如果是父菜单，不导航
    if (key === '/copy-trading-management') {
      return
    }
    
    // 处理退出登录
    if (key === 'logout') {
      handleLogoutConfirm()
      return
    }
    
    navigate(key)
    if (isMobile) {
      setMobileMenuOpen(false)
    }
  }
  
  const handleOpenChange = (keys: string[]) => {
    setOpenKeys(keys)
  }
  
  if (isMobile) {
    // 移动端布局
    return (
      <AntLayout style={{ minHeight: '100vh' }}>
        <Header style={{ 
          background: '#001529', 
          padding: '0 16px',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between'
        }}>
          <div style={{ color: '#fff', fontSize: '18px', fontWeight: 'bold' }}>
            PolyHermes
          </div>
          <Button
            type="text"
            icon={<MenuOutlined />}
            style={{ color: '#fff' }}
            onClick={() => setMobileMenuOpen(true)}
          />
        </Header>
        <Content style={{ 
          padding: '12px 8px', 
          background: '#f0f2f5',
          minHeight: 'calc(100vh - 64px)'
        }}>
          {children}
        </Content>
        <Drawer
          title="导航菜单"
          placement="left"
          onClose={() => setMobileMenuOpen(false)}
          open={mobileMenuOpen}
          bodyStyle={{ padding: 0 }}
        >
          <Menu
            mode="inline"
            selectedKeys={getSelectedKeys()}
            openKeys={openKeys}
            onOpenChange={handleOpenChange}
            items={menuItems}
            onClick={handleMenuClick}
            style={{ border: 'none' }}
          />
        </Drawer>
      </AntLayout>
    )
  }
  
  // 桌面端布局
  return (
    <AntLayout style={{ height: '100vh', overflow: 'hidden' }}>
      <Sider 
        width={200} 
        style={{ 
          background: '#001529',
          height: '100vh',
          position: 'fixed',
          left: 0,
          top: 0,
          overflow: 'hidden'
        }}
      >
        <div style={{ 
          height: '64px', 
          display: 'flex', 
          alignItems: 'center', 
          justifyContent: 'center',
          color: '#fff',
          fontSize: '18px',
          fontWeight: 'bold',
          flexShrink: 0
        }}>
          PolyHermes
        </div>
        <Menu
          mode="inline"
          selectedKeys={getSelectedKeys()}
          openKeys={openKeys}
          onOpenChange={handleOpenChange}
          items={menuItems}
          onClick={handleMenuClick}
          style={{ 
            height: 'calc(100vh - 64px)', 
            borderRight: 0,
            overflowY: 'auto'
          }}
        />
      </Sider>
      <AntLayout style={{ marginLeft: 200, height: '100vh' }}>
        <Content style={{ 
          padding: '24px', 
          background: '#f0f2f5', 
          height: '100vh',
          overflowY: 'auto'
        }}>
          {children}
        </Content>
      </AntLayout>
    </AntLayout>
  )
}

export default Layout

