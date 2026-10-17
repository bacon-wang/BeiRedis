import org.junit.platform.launcher.core.LauncherDiscoveryRequestBuilder;
import org.junit.platform.launcher.core.LauncherFactory;
import org.junit.platform.launcher.listeners.SummaryGeneratingListener;
import org.junit.platform.launcher.listeners.TestExecutionSummary;

import java.io.PrintWriter;

import static org.junit.platform.engine.discovery.DiscoverySelectors.selectPackage;

// 运行 redis 包下的全部测试，打印汇总和失败详情；有失败时退出码为 1
public class RunTests {
    public static void main(String[] args) {
        var request = LauncherDiscoveryRequestBuilder.request().selectors(selectPackage("redis")).build();
        var listener = new SummaryGeneratingListener();
        LauncherFactory.create().execute(request, listener);
        TestExecutionSummary s = listener.getSummary();
        PrintWriter out = new PrintWriter(System.out, true);
        s.printTo(out);
        s.printFailuresTo(out, 10);
        System.exit(s.getTotalFailureCount() == 0 ? 0 : 1);
    }
}
