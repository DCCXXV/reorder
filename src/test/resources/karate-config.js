function fn() {
    var env = karate.env;
    karate.log('karate.env system property was:', env);

    if (!env) {
        env = 'dev';
    }

    var config = {
        env: env,
        myVarName: 'someValue',
        baseUrl: 'http://localhost:8080'
    }

    karate.configure('headers', {'X-Test-Framework': 'Karate'});

    karate.configure('driver', {
        type: 'chrome',
        // descomentar para linux
        // executable: '/usr/bin/chromium-browser',
        executable: "C:\\Program Files\\Google\\Chrome\\Application\\chrome.exe",
        addOptions: ["--remote-allow-origins=*", "--incognito"],
        showDriverLog: true
    })

    karate.configure('connectTimeout', 5000);
    karate.configure('readTimeout', 5000);

    if (env == 'dev') {
        // para dev
    } else if (env == 'e2e') {
        // para e2e
    }

    return config;
}
