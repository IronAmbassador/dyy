#!/bin/bash

# Android 新闻应用 - 快速测试脚本
# =======================================

echo "═══════════════════════════════════════════════════════════════════════"
echo "                Android 新闻应用 - 快速测试脚本"
echo "═══════════════════════════════════════════════════════════════════════"
echo ""

# 颜色定义
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# 检查 Gradle wrapper 是否存在
if [ ! -f "./gradlew" ]; then
    echo -e "${RED}错误: 找不到 gradlew 文件，请在项目根目录运行此脚本${NC}"
    exit 1
fi

# 使 gradlew 可执行
chmod +x ./gradlew 2>/dev/null

show_menu() {
    echo -e "${BLUE}请选择要执行的测试:${NC}"
    echo ""
    echo "  1) 运行所有单元测试"
    echo "  2) 运行数据库测试"
    echo "  3) 运行实体类测试"
    echo "  4) 运行设计方法测试 (等价类 + 正交表)"
    echo "  5) 运行集成测试"
    echo "  6) 运行 API 测试"
    echo "  7) 生成测试报告"
    echo "  8) 清理并运行所有测试"
    echo "  9) 查看测试报告"
    echo "  0) 退出"
    echo ""
    read -p "请输入选项 (0-9): " choice
}

run_all_tests() {
    echo -e "${GREEN}正在运行所有单元测试...${NC}"
    echo ""
    ./gradlew test
    show_test_result
}

run_db_tests() {
    echo -e "${GREEN}正在运行数据库测试...${NC}"
    echo ""
    ./gradlew test --tests "com.white.news.db.*"
    show_test_result
}

run_entity_tests() {
    echo -e "${GREEN}正在运行实体类测试...${NC}"
    echo ""
    ./gradlew test --tests "com.white.news.entity.*"
    show_test_result
}

run_design_tests() {
    echo -e "${GREEN}正在运行设计方法测试...${NC}"
    echo ""
    echo "  - 等价类划分测试 (注册功能)"
    echo "  - 正交表测试 (登录功能)"
    echo ""
    ./gradlew test --tests "com.white.news.testdesign.*"
    show_test_result
}

run_integration_tests() {
    echo -e "${GREEN}正在运行集成测试...${NC}"
    echo ""
    ./gradlew test --tests "com.white.news.UserFlowIntegrationTest"
    show_test_result
}

run_api_tests() {
    echo -e "${GREEN}正在运行 API 测试...${NC}"
    echo ""
    echo "注意: 需要网络连接和有效的 API Key"
    echo ""
    ./gradlew test --tests "com.white.news.api.NewsApiTest"
    show_test_result
}

generate_reports() {
    echo -e "${GREEN}正在生成测试报告...${NC}"
    echo ""
    ./gradlew test jacocoTestReport
    echo ""
    echo -e "${GREEN}报告生成完成!${NC}"
    echo ""
    echo -e "  测试报告: ${YELLOW}app/build/reports/tests/test/index.html${NC}"
    echo -e "  覆盖报告: ${YELLOW}app/build/reports/jacoco/jacocoTestReport/html/index.html${NC}"
}

clean_and_test() {
    echo -e "${GREEN}正在清理并运行所有测试...${NC}"
    echo ""
    ./gradlew clean test jacocoTestReport
    show_test_result
}

view_reports() {
    local test_report="app/build/reports/tests/test/index.html"
    local jacoco_report="app/build/reports/jacoco/jacocoTestReport/html/index.html"

    if [ -f "$test_report" ]; then
        echo -e "${GREEN}正在打开测试报告...${NC}"
        if command -v xdg-open &> /dev/null; then
            xdg-open "$test_report"
        elif command -v open &> /dev/null; then
            open "$test_report"
        elif command -v start &> /dev/null; then
            start "$test_report"
        else
            echo "无法自动打开报告，请手动打开: $test_report"
        fi
    else
        echo -e "${RED}测试报告不存在，请先运行测试!${NC}"
    fi
}

show_test_result() {
    echo ""
    echo "═══════════════════════════════════════════════════════════════════════"
    if [ $? -eq 0 ]; then
        echo -e "${GREEN}✓ 测试运行完成!${NC}"
    else
        echo -e "${RED}✗ 测试运行出现错误!${NC}"
    fi
    echo "═══════════════════════════════════════════════════════════════════════"
    echo ""
    echo -e "${YELLOW}提示: 在浏览器中查看详细报告${NC}"
    echo -e "      ${BLUE}app/build/reports/tests/test/index.html${NC}"
    echo ""
}

show_help() {
    echo ""
    echo "快速测试脚本使用说明:"
    echo ""
    echo "  直接运行 ./test.sh 进入交互式菜单"
    echo ""
    echo "  也可以直接传入命令行参数:"
    echo "    ./test.sh all      - 运行所有测试"
    echo "    ./test.sh db       - 运行数据库测试"
    echo "    ./test.sh entity   - 运行实体测试"
    echo "    ./test.sh design   - 运行设计方法测试"
    echo "    ./test.sh integration - 运行集成测试"
    echo "    ./test.sh api      - 运行 API 测试"
    echo "    ./test.sh report   - 生成报告"
    echo "    ./test.sh clean    - 清理并测试"
    echo "    ./test.sh view     - 查看报告"
    echo ""
}

# 主程序
if [ $# -gt 0 ]; then
    case $1 in
        all)
            run_all_tests
            ;;
        db)
            run_db_tests
            ;;
        entity)
            run_entity_tests
            ;;
        design)
            run_design_tests
            ;;
        integration)
            run_integration_tests
            ;;
        api)
            run_api_tests
            ;;
        report)
            generate_reports
            ;;
        clean)
            clean_and_test
            ;;
        view)
            view_reports
            ;;
        help)
            show_help
            ;;
        *)
            echo -e "${RED}未知选项: $1${NC}"
            show_help
            ;;
    esac
else
    while true; do
        show_menu
        case $choice in
            1)
                run_all_tests
                ;;
            2)
                run_db_tests
                ;;
            3)
                run_entity_tests
                ;;
            4)
                run_design_tests
                ;;
            5)
                run_integration_tests
                ;;
            6)
                run_api_tests
                ;;
            7)
                generate_reports
                ;;
            8)
                clean_and_test
                ;;
            9)
                view_reports
                ;;
            0)
                echo ""
                echo "感谢使用，再见!"
                echo ""
                exit 0
                ;;
            *)
                echo -e "${RED}无效选项，请重新选择!${NC}"
                echo ""
                ;;
        esac
        read -p "按 Enter 继续..."
    done
fi
