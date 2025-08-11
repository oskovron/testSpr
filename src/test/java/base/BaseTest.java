package base;

import api.requests.PlayerApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.Listeners;

import io.qameta.allure.testng.AllureTestNg;
import listeners.AllureEnvironmentListener;

import java.util.List;

@Listeners({AllureTestNg.class, AllureEnvironmentListener.class})
public abstract class BaseTest {
    protected PlayerApiClient apiClient;
    protected List<Integer> createdPlayerIds;

    @BeforeSuite(alwaysRun = true)
    public void globalSetup() {
        // Global test hooks can be placed here if needed.
    }
}


