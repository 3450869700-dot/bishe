from selenium import webdriver
from selenium.webdriver.chrome.service import Service
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
import time

# 配置Chrome选项
chrome_options = Options()
# chrome_options.add_argument("--headless")  # 禁用无头模式，显示浏览器窗口以便调试
chrome_options.add_argument("--window-size=1920,1080")
chrome_options.add_argument("--disable-gpu")

# 初始化浏览器
try:
    service = Service()
    driver = webdriver.Chrome(service=service, options=chrome_options)
    driver.implicitly_wait(10)
    print("浏览器初始化成功")
except Exception as e:
    print(f"浏览器初始化失败：{str(e)}")
    exit(1)

# 访问惠农网搜索页面
keyword = "桃"
search_url = f"https://www.cnhnb.com/supply/search/?k={keyword}"
print(f"访问URL：{search_url}")
driver.get(search_url)

# 等待搜索结果加载
WebDriverWait(driver, 15).until(
    EC.presence_of_element_located((By.XPATH, '//div[@class="supply-item"]'))
)

# 打印当前页面URL和标题
print(f"当前页面URL：{driver.current_url}")
print(f"当前页面标题：{driver.title}")

# 尝试翻页
for i in range(3):
    print(f"\n--- 第 {i+1} 次翻页尝试 ---")
    
    # 先滚动加载
    for scroll in range(2):
        driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")
        time.sleep(1)
    
    # 寻找下一页按钮
    next_page_button = None
    try:
        # 尝试多种方式找下一页按钮
        next_page_button = driver.find_element(By.XPATH, '//a[contains(@class, "next")]')
        print("找到下一页按钮")
        
        # 打印按钮的href属性
        href = next_page_button.get_attribute("href")
        print(f"下一页按钮href：{href}")
        
        # 点击下一页
        driver.execute_script("arguments[0].scrollIntoView(true);", next_page_button)
        time.sleep(1)
        next_page_button.click()
        print("点击下一页按钮")
        
        # 等待新页面加载
        time.sleep(3)
        
        # 打印当前页面URL和标题
        print(f"翻页后URL：{driver.current_url}")
        print(f"翻页后标题：{driver.title}")
        
        # 检查是否有商品列表
        product_items = driver.find_elements(By.XPATH, '//div[@class="supply-item"]')
        print(f"翻页后找到 {len(product_items)} 个商品项")
        
        # 提取前几个商品的URL
        if product_items:
            for j in range(min(3, len(product_items))):
                link = product_items[j].find_element(By.TAG_NAME, 'a')
                product_url = link.get_attribute("href")
                print(f"商品 {j+1} URL：{product_url}")
    except Exception as e:
        print(f"翻页失败：{str(e)}")
        break

driver.quit()
