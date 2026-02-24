import time
import random
import re
from selenium import webdriver
from selenium.webdriver.chrome.options import Options
from selenium.webdriver.common.by import By
from selenium.webdriver.support.ui import WebDriverWait
from selenium.webdriver.support import expected_conditions as EC
from lxml import html
import pandas as pd

# ---------------------- 配置项 ----------------------
KEYWORD = "桃"  # 搜索关键词
MAX_PAGES = 200  # 最大爬取页数（防止无限循环）
MAX_ITEMS = 300  # 最大爬取商品条数
SCROLL_TIMES = 5  # 每页滚动加载次数，每次滚动加载更多商品
DELAY_MIN = 2  # 延迟最小值
DELAY_MAX = 4  # 延迟最大值
# ---------------------------------------------------

# 设置Chrome选项
chrome_options = Options()
# 禁用无头模式，显示浏览器窗口以便调试
# chrome_options.add_argument("--headless")
chrome_options.add_argument("--window-size=1920,1080")
chrome_options.add_argument("--disable-gpu")
chrome_options.add_argument(f"--user-agent=Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/143.0.0.0 Safari/537.36")
# 禁用图片加载，加快页面加载速度
prefs = {
    'profile.default_content_setting_values': {
        'images': 2
    }
}
chrome_options.add_experimental_option('prefs', prefs)

# 初始化浏览器
print("正在初始化浏览器...")

try:
    from selenium.webdriver.chrome.service import Service
    
    # 直接使用Service类，不依赖webdriver_manager
    # Selenium会自动查找系统中的ChromeDriver
    print("正在初始化Chrome浏览器...")
    service = Service()
    driver = webdriver.Chrome(service=service, options=chrome_options)
    driver.implicitly_wait(10)  # 设置隐式等待
    print("浏览器初始化成功")
except Exception as e:
    import traceback
    print(f"浏览器初始化失败：{str(e)}")
    traceback.print_exc()
    print(f"请确保Chrome浏览器和ChromeDriver已正确安装")
    print(f"ChromeDriver应该位于系统环境变量PATH中，或与脚本同一目录")
    exit(1)

# 存储爬取到的商品数据
all_product_data = []
# 存储已爬取的商品ID，用于去重
crawled_product_ids = set()

# 1. 访问惠农网搜索页面
search_url = f"https://www.cnhnb.com/supply/search/?k={KEYWORD}"
print(f"正在访问搜索页面：{search_url}")
driver.get(search_url)

# 等待搜索结果加载完成
WebDriverWait(driver, 15).until(
    EC.presence_of_element_located((By.XPATH, '//div[@class="supply-item"]'))
)

# 2. 循环爬取多页
print("惠农网Selenium爬虫开始运行...")
print(f"关键词：{KEYWORD}")
print(f"计划爬取：最多 {MAX_ITEMS} 条商品数据")

for page in range(1, MAX_PAGES + 1):
    # 检查是否已达到最大爬取条数
    if len(all_product_data) >= MAX_ITEMS:
        print(f"\n已达到最大爬取条数 {MAX_ITEMS}，停止爬取")
        break
        
    print(f"\n=== 正在爬取第 {page}/{MAX_PAGES} 页 ===")
    print(f"当前已爬取：{len(all_product_data)}/{MAX_ITEMS} 条")
    
    # 2.1 模拟滚动加载更多商品
    print(f"\n第 {page} 页：开始滚动加载商品...")
    for scroll in range(SCROLL_TIMES):
        print(f"  第 {scroll + 1} 次滚动...")
        # 滚动到底部
        driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")
        # 随机延迟，模拟人工操作
        scroll_delay = random.uniform(1, 2)
        time.sleep(scroll_delay)
    
    # 额外延迟，确保所有商品都已加载
    time.sleep(random.uniform(DELAY_MIN, DELAY_MAX))
    
    # 2.2 提取当前页面中的所有商品链接
    print(f"第 {page} 页：开始提取商品链接...")
    page_source = driver.page_source
    tree = html.fromstring(page_source)
    
    # 提取所有商品项
    product_items = tree.xpath('//div[@class="supply-item"]')
    print(f"第 {page} 页：找到 {len(product_items)} 个商品项")
    
    # 提取当前页面的商品链接
    current_page_product_links = []
    for item in product_items:
        # 提取商品链接
        detail_url = item.xpath('.//a/@href')
        if not detail_url:
            continue
        
        detail_url = detail_url[0].strip()
        if not detail_url.startswith('http'):
            if detail_url.startswith('//'):
                detail_url = 'https:' + detail_url
            else:
                detail_url = 'https://www.cnhnb.com' + detail_url
        
        # 提取商品ID
        product_id = None
        # 尝试从URL中提取商品ID
        product_id_match = re.search(r'/gongying/(\d+)/', detail_url)
        if product_id_match:
            product_id = product_id_match.group(1)
        else:
            # 如果无法从URL提取，使用URL的哈希值作为ID
            product_id = str(hash(detail_url))
        
        # 只添加未爬取过的商品
        if product_id not in crawled_product_ids:
            current_page_product_links.append((product_id, detail_url))
            crawled_product_ids.add(product_id)
    
    print(f"第 {page} 页：提取到 {len(current_page_product_links)} 个新商品")
    
    # 2.3 爬取当前页面的所有新商品详情
    for idx, (product_id, detail_url) in enumerate(current_page_product_links, 1):
        # 检查是否已达到最大爬取条数
        if len(all_product_data) >= MAX_ITEMS:
            print(f"\n已达到最大爬取条数 {MAX_ITEMS}，停止爬取")
            break
            
        print(f"  正在爬取商品 {idx}/{len(current_page_product_links)}: {detail_url}")
        
        try:
            # 访问详情页
            driver.get(detail_url)
            time.sleep(random.uniform(DELAY_MIN, DELAY_MAX))
            
            # 等待页面加载完成
            WebDriverWait(driver, 15).until(
                EC.presence_of_element_located((By.XPATH, '//h1'))
            )
            
            # 提取详情页信息
            detail_page = driver.page_source
            detail_tree = html.fromstring(detail_page)
            
            # 商品名称
            product_name = detail_tree.xpath('//h1[@class="d-t"]/text() | //h1/text()')
            product_name = ''.join([x.strip() for x in product_name if x.strip()]) if product_name else '无'
            
            # 店铺名称
            shop = '无'
            shop_selectors = [
                '//div[@class="head"]/text()',
                '//div[@class="shop-name"]/a/text()',
                '//div[@class="shop-info"]//a/text()',
                '//div[contains(@class, "shop")]/text()',
                '//a[contains(@class, "shop")]/text()',
                '//div[@class="store-box"]//div[@class="head"]/text()'
            ]
            
            for selector in shop_selectors:
                shops = detail_tree.xpath(selector)
                if shops:
                    valid_shops = [s.strip() for s in shops if s.strip()]
                    if valid_shops:
                        shop = valid_shops[0]
                        break
            
            # 图片链接
            image_url = '无'
            image_selectors = [
                '//div[@class="show-ctn"]//img/@src',
                '//div[contains(@class, "zoom")]//img/@src',
                '//div[@class="gallery"]//img/@src',
                '//div[@class="product-image"]//img/@src',
                '//img[@class="product-img"]/@src',
                '//div[@class="swiper-wrapper"]//img/@src',
                '//div[@class="carousel"]//img/@src'
            ]
            
            for selector in image_selectors:
                images = detail_tree.xpath(selector)
                if images:
                    for img in images:
                        if isinstance(img, str) and img.strip() and not img.strip() == 'none':
                            image_url = img.strip()
                            break
                    if image_url != '无':
                        break
            
            # 处理图片链接相对路径
            if image_url != '无' and not image_url.startswith('http'):
                if image_url.startswith('//'):
                    image_url = 'https:' + image_url
                elif image_url.startswith('/'):
                    image_url = 'https://www.cnhnb.com' + image_url
            
            # 发货地址
            shipping_address = '无'
            address_selectors = [
                '//div[@class="contact"]/text()',
                '//div[@class="address"]/text()',
                '//span[@class="location"]/text()',
                '//div[contains(@class, "location")]/text()',
                '//div[contains(text(), "发货地址")]/following-sibling::div/text()',
                '//div[contains(text(), "产地")]/following-sibling::div/text()',
                '//*[contains(text(), "发货地址")]/parent::div/following-sibling::div/text()',
                '//*[contains(text(), "产地")]/parent::div/following-sibling::div/text()'
            ]
            
            for selector in address_selectors:
                addresses = detail_tree.xpath(selector)
                if addresses:
                    valid_addresses = [addr.strip() for addr in addresses if addr.strip()]
                    if valid_addresses:
                        shipping_address = valid_addresses[0]
                        break
            
            # 规格列表
            specs_list = []
            spec_items = detail_tree.xpath('//div[@class="spec-items flex-c"]')
            for spec_item in spec_items:
                spec_name = spec_item.xpath('.//div[@class="s-name"]/text()')
                spec_price = spec_item.xpath('.//div[@class="s-price"]/text()')
                spec_unit = spec_item.xpath('.//div[@class="s-unit"]/text()')
                
                if spec_name and spec_price and spec_unit:
                    spec_name = spec_name[0].strip()
                    spec_price = spec_price[0].strip()
                    spec_unit = spec_unit[0].strip()
                    specs_list.append({
                        '规格名称': spec_name,
                        '价格': spec_price,
                        '起批量': spec_unit
                    })
            
            # 如果没有找到规格列表，尝试提取默认价格
            if not specs_list:
                # 提取价格
                price = detail_tree.xpath('//div[@class="active-p"]/text() | //div[contains(@class, "price")]//text()')
                price = ''.join([x.strip() for x in price if x.strip() and "元" in x]) if price else '0'
                price_match = re.search(r'[0-9]+(\.[0-9]+)?', price)
                if price_match:
                    price = price_match.group(0)
                
                # 添加默认规格
                specs_list.append({
                    '规格名称': '默认规格',
                    '价格': price,
                    '起批量': '无'
                })
            
            # 提取其他信息：单颗重、货品包装、品种名、货品等级、采购热度
            single_weight = '无'
            bundling_type = '无'
            variety_name = '无'
            grade_name = '无'
            purchase_heat = '无'
            
            # 提取页面文本，用于正则匹配
            page_text = ''.join([x.strip() for x in detail_tree.xpath('//body//text()')])
            
            # 改进的信息提取方法，结合多种选择器和正则表达式
            
            # 单颗重提取
            weight_patterns = [
                r'单颗重[：:]?\s*([^\s|]+)',
                r'单果重[：:]?\s*([^\s|]+)',
                r'果重[：:]?\s*([^\s|]+)',
                r'单果重量[：:]?\s*([^\s|]+)'
            ]
            
            # 先尝试结构化提取
            struct_selectors = [
                ('单颗重', './/div[contains(text(), "单颗重")]/following-sibling::div/text()', '单颗重'),
                ('单果重', './/div[contains(text(), "单果重")]/following-sibling::div/text()', '单果重'),
                ('果重', './/div[contains(text(), "果重")]/following-sibling::div/text()', '单果重'),
                ('包装', './/div[contains(text(), "包装")]/following-sibling::div/text()', '包装'),
                ('品种', './/div[contains(text(), "品种")]/following-sibling::div/text()', '品种'),
                ('等级', './/div[contains(text(), "等级")]/following-sibling::div/text()', '等级'),
            ]
            
            for label, selector, field in struct_selectors:
                values = detail_tree.xpath(selector)
                if values:
                    value = ''.join([v.strip() for v in values if v.strip()])
                    if value and field == '单颗重':
                        single_weight = value
                    elif value and field == '包装':
                        bundling_type = value
                    elif value and field == '品种':
                        variety_name = value
                    elif value and field == '等级':
                        grade_name = value
            
            # 如果结构化提取失败，使用正则提取
            if single_weight == '无':
                for pattern in weight_patterns:
                    match = re.search(pattern, page_text)
                    if match:
                        single_weight = match.group(1).strip()
                        break
            
            # 货品包装提取
            if bundling_type == '无':
                package_patterns = [
                    r'货品包装[：:]?\s*([^\s|]+)',
                    r'包装[：:]?\s*([^\s|]+)',
                    r'包装方式[：:]?\s*([^\s|]+)'
                ]
                for pattern in package_patterns:
                    match = re.search(pattern, page_text)
                    if match:
                        bundling_type = match.group(1).strip()
                        break
            
            # 品种名提取
            if variety_name == '无':
                variety_patterns = [
                    r'品种名[：:]?\s*([^\s|]+)',
                    r'品种[：:]?\s*([^\s|]+)',
                    r'产品品种[：:]?\s*([^\s|]+)'
                ]
                for pattern in variety_patterns:
                    match = re.search(pattern, page_text)
                    if match:
                        variety_name = match.group(1).strip()
                        break
            
            # 货品等级提取
            if grade_name == '无':
                grade_patterns = [
                    r'货品等级[：:]?\s*([^\s|]+)',
                    r'等级[：:]?\s*([^\s|]+)',
                    r'产品等级[：:]?\s*([^\s|]+)'
                ]
                for pattern in grade_patterns:
                    match = re.search(pattern, page_text)
                    if match:
                        grade_name = match.group(1).strip()
                        break
            
            # 采购热度提取
            heat_patterns = [
                r'(\d+)\s*询价',
                r'询价\s*(\d+)',
                r'(\d+)\s*已询价',
                r'已询价\s*(\d+)',
                r'(\d+)\s*热度',
                r'热度\s*(\d+)',
                r'(\d+)\s*采购',
                r'采购\s*(\d+)',
                r'询价数[：:]?\s*(\d+)',
                r'浏览量[：:]?\s*(\d+)'
            ]
            
            for pattern in heat_patterns:
                match = re.search(pattern, page_text)
                if match:
                    purchase_heat = match.group(1)
                    break
            
            # 保存商品数据
            product_data = {
                '商品名称': product_name,
                '单颗重': single_weight,
                '货品包装': bundling_type,
                '品种名': variety_name,
                '货品等级': grade_name,
                '价格': specs_list[0]['价格'],  # 默认显示第一个规格的价格
                '规格列表': specs_list,
                '采购热度': purchase_heat,
                '店铺名称': shop,
                '图片链接': image_url,
                '发货地址': shipping_address,
                '详情页URL': detail_url
            }
            
            all_product_data.append(product_data)
            print(f"  商品爬取成功：{product_name}")
            print(f"  当前已爬取：{len(all_product_data)}/{MAX_ITEMS} 条")
            
        except Exception as e:
            print(f"  商品爬取失败：{str(e)}")
            continue
    
    # 检查是否已达到最大爬取条数
    if len(all_product_data) >= MAX_ITEMS:
        print(f"\n已达到最大爬取条数 {MAX_ITEMS}，停止爬取")
        break
    
    # 2.4 翻页处理
    print(f"第 {page} 页：开始翻页...")
    
    # 记录当前页面的商品ID列表，用于判断是否加载了新内容
    current_product_ids = set()
    current_items = driver.find_elements(By.XPATH, '//div[@class="supply-item"]')
    for item in current_items:
        try:
            link = item.find_element(By.TAG_NAME, 'a')
            href = link.get_attribute('href')
            if href:
                product_id_match = re.search(r'/gongying/(\d+)/', href)
                if product_id_match:
                    current_product_ids.add(product_id_match.group(1))
        except:
            pass
    
    try:
        # 寻找下一页按钮，使用更灵活的选择器
        # 尝试多种定位方式
        next_page_button = None
        
        # 方式1：直接寻找带有next类的元素（惠农网主要使用这种方式）
        try:
            next_page_button = driver.find_element(By.XPATH, '//a[contains(@class, "next")]')
            print("  使用class定位找到下一页按钮")
        except:
            pass
        
        # 方式2：直接寻找包含"下一页"文本的链接
        if not next_page_button:
            try:
                next_page_button = driver.find_element(By.XPATH, '//a[contains(text(), "下一页")]')
                print("  使用文本定位找到下一页按钮")
            except:
                pass
        
        # 方式3：寻找分页区域中的下一页按钮
        if not next_page_button:
            try:
                # 寻找可能的分页容器
                pagination_containers = driver.find_elements(By.XPATH, '//div[contains(@class, "pagination") or contains(@class, "pager") or contains(@class, "page") or contains(@class, "paging")]')
                for container in pagination_containers:
                    try:
                        next_btn = container.find_element(By.XPATH, './/a[contains(text(), "下一页") or contains(@class, "next")]')
                        if next_btn:
                            next_page_button = next_btn
                            print("  使用分页区域定位找到下一页按钮")
                            break
                    except:
                        continue
            except:
                pass
        
        # 方式4：寻找所有链接，然后筛选包含"下一页"的
        if not next_page_button:
            try:
                all_links = driver.find_elements(By.TAG_NAME, 'a')
                for link in all_links:
                    try:
                        if "下一页" in link.text:
                            next_page_button = link
                            print("  使用所有链接筛选找到下一页按钮")
                            break
                    except:
                        continue
            except:
                pass
        
        if next_page_button:
            # 滚动到下一页按钮可见位置
            driver.execute_script("arguments[0].scrollIntoView(true);", next_page_button)
            time.sleep(random.uniform(DELAY_MIN, DELAY_MAX))
            
            # 点击下一页 - 使用JavaScript点击，避免可能的问题
            print(f"  准备点击下一页按钮")
            driver.execute_script("arguments[0].click();", next_page_button)
            print(f"  下一页按钮点击完成")
            
            # 等待新内容加载
            print(f"  等待新内容加载...")
            
            # 等待商品列表重新加载
            WebDriverWait(driver, 15).until(
                EC.staleness_of(current_items[0]) if current_items else EC.presence_of_element_located((By.XPATH, '//div[@class="supply-item"]'))
            )
            
            # 等待新商品出现
            WebDriverWait(driver, 15).until(
                EC.presence_of_element_located((By.XPATH, '//div[@class="supply-item"]'))
            )
            
            # 额外延迟，确保页面完全加载
            time.sleep(random.uniform(DELAY_MIN, DELAY_MAX))
            
            # 检查是否加载了新商品
            new_items = driver.find_elements(By.XPATH, '//div[@class="supply-item"]')
            new_products_loaded = False
            for item in new_items[:5]:  # 检查前5个商品
                try:
                    link = item.find_element(By.TAG_NAME, 'a')
                    href = link.get_attribute('href')
                    if href:
                        product_id_match = re.search(r'/gongying/(\d+)/', href)
                        if product_id_match and product_id_match.group(1) not in current_product_ids:
                            new_products_loaded = True
                            break
                except:
                    continue
            
            if new_products_loaded:
                print(f"第 {page} 页：成功进入下一页，加载到新商品")
            else:
                print(f"第 {page} 页：下一页点击成功，但可能没有新商品加载")
        else:
            print(f"第 {page} 页：无法找到下一页按钮")
            print(f"  惠农网可能使用动态加载，尝试滚动加载更多商品")
            
            # 尝试滚动加载更多商品
            last_height = driver.execute_script("return document.body.scrollHeight")
            driver.execute_script("window.scrollTo(0, document.body.scrollHeight);")
            time.sleep(random.uniform(DELAY_MIN + 1, DELAY_MAX + 2))
            
            # 检查是否有新内容加载
            new_height = driver.execute_script("return document.body.scrollHeight")
            if new_height > last_height:
                print(f"  滚动加载成功，页面高度从 {last_height} 增加到 {new_height}")
            else:
                print(f"  滚动加载没有新内容")
            
            # 等待可能的动态加载
            time.sleep(random.uniform(DELAY_MIN, DELAY_MAX))
            
    except Exception as e:
        print(f"第 {page} 页：翻页失败: {str(e)}")
        print(f"第 {page} 页：继续爬取下一页，不停止")
        # 不停止，继续尝试下一页
        # 添加延迟，避免过快请求
        time.sleep(random.uniform(DELAY_MIN + 1, DELAY_MAX + 2))

# 3. 关闭浏览器
driver.quit()

# 4. 处理数据并导出到Excel
print(f"\n=== 爬取完成 ===")
print(f"共爬取 {len(all_product_data)} 条商品数据")

if all_product_data:
    # 处理数据，展开规格列表
    export_data = []
    for product in all_product_data:
        # 如果有多个规格，每个规格生成一行数据
        if len(product['规格列表']) > 1:
            for spec in product['规格列表']:
                export_row = product.copy()
                export_row['价格'] = spec['价格']
                export_row['规格'] = spec['规格名称']
                export_row['起批量'] = spec['起批量']
                export_row.pop('规格列表')  # 移除规格列表字段
                export_data.append(export_row)
        else:
            # 只有一个规格，直接使用
            export_row = product.copy()
            export_row['规格'] = product['规格列表'][0]['规格名称']
            export_row['起批量'] = product['规格列表'][0]['起批量']
            export_row.pop('规格列表')  # 移除规格列表字段
            export_data.append(export_row)
    
    # 转换为DataFrame
    df = pd.DataFrame(export_data)
    
    # 调整列顺序
    column_order = [
        '商品名称', '店铺名称', '单颗重', '货品包装', '品种名', 
        '货品等级', '规格', '价格', '起批量', '采购热度', 
        '发货地址', '图片链接', '详情页URL'
    ]
    
    # 确保所有列都存在
    for col in column_order:
        if col not in df.columns:
            df[col] = '无'
    
    df = df[column_order]
    
    # 生成带时间戳的文件名
    from datetime import datetime
    timestamp = datetime.now().strftime("%Y%m%d_%H%M%S")
    excel_filename = f"cnhnb_selenium_improved_data_{timestamp}.xlsx"
    
    # 导出到Excel
    try:
        df.to_excel(excel_filename, index=False, engine='openpyxl')
        print(f"\n✅ 数据保存完成！共 {len(df)} 行数据，文件：{excel_filename}")
    except Exception as e:
        print(f"\n❌ 保存Excel文件失败：{str(e)}")
        # 尝试使用另一个文件名
        backup_filename = f"cnhnb_selenium_improved_data_{timestamp}_backup.xlsx"
        try:
            df.to_excel(backup_filename, index=False, engine='openpyxl')
            print(f"✅ 数据已保存到备份文件：{backup_filename}")
        except Exception as backup_e:
            print(f"❌ 备份文件保存也失败：{str(backup_e)}")
else:
    print("\n❌ 未爬取到任何商品数据")

print("\n爬虫任务完成！")