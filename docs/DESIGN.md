# Cosmic Agent - Design Log

Kotlin rewrite of `Blended-XP/src/questManager/CosmosAgent` (C#). Reference only: understand it, then write it fresh.

## Decisions made

| # | Decision | Why |
|---|----------|-----|
| D1 | Kotlin + Gradle, built in Android Studio | Official docs/samples are Kotlin; better debugging |
| D2 | Device admin only (no device owner) | Device owner is not realistic on Quest right now; admin covers lock + uninstall protection |
| D3 | Java SignalR client (`com.microsoft.signalr`) | Same hub (`HeadsetControlHub`), no server change |
| D4 | App switching happens only on SignalR commands from LocalServer | No on-device app-switch logic except the new auto-launch |
| D5 | Package `com.blendedxr.gls.agent` (namespace + applicationId match). Old agent deprovisioned, so no in-place upgrade needed | Ghostlight Studios naming |
| D6 | Project lives at `~/AndroidStudioProjects/ControlAgent` (not Cosmic_Agent) | Where the IDE created it |
| D7 | Own private repo for now; later copy/clone into Blended-XP and delete the C# CosmosAgent files | Keeps the rewrite isolated until it reaches parity |
| D8 | Plain Android Views (XML layout), no Compose, no XR libs. Status screen only | Q9: smallest footprint; UI is incidental to the agent |
| D9 | Accessibility service stays: it is what lets the agent switch apps without an embedded library in each app. Gestures (`canPerformGestures`) dropped for now | Q1: proven in earlier testing |
| D10 | Dev builds must be easy to remove (device admin not permanent). Release may differ | Q6 (dev side) |
| D12 | Launching apps goes through the accessibility service (`AgentAccessibilityService.launchPackage`), reached from other code via a static `instance` for now. `AgentService` never calls `startActivity` itself | Plain background services are blocked (BAL). Static instance is the smallest thing that works, see Q13 |
| D13 | POC auto-launch target is hardcoded (`com.ghostlight.novaxr`) with a fixed 20 s delay after the service starts. Both are placeholders for config (Q3) and a real readiness signal (Q4) | Prove the path first, configure later |
| D11 | Code style: modular, package-by-feature, thin Android components with logic in plain Kotlin classes, conventional Android/Kotlin layout. Reference: Google's Now in Android sample and the official app-architecture guide | Readability/maintainability for a human owner |

## Goal beyond parity

Same behaviour as the C# agent, plus: auto-launch a default app after boot and keep it in place (configurable).

## Stages

1. Skeleton installs on headset, shows in Unknown Sources (DONE, verified on headset)
2. Permissions + capability screen + provisioning script (adb grants). Done so far: device admin + capability checker + refresh button + accessibility service (enabled over adb, row working). E1 done (see Findings). Next: E2 (VR window events), then the adb-grant provisioning script. `WRITE_SECURE_SETTINGS` deferred with Q12, `PACKAGE_USAGE_STATS` waits on E2, `SYSTEM_ALERT_WINDOW` looks droppable
3. Boot receiver + foreground service, survives reboot (DONE 2026-10-12, verified on headset, see Stage 3 status below)
4. Hub connection (Hello, reconnect, telemetry)
5. Commands (Launch, GoHome, Lock) + accessibility service
6. Configurability (settings, auto-launch default app)

### Stage 3 status (2026-10-12)

Done and verified on the headset (SDK 34), by rebooting:
- `BootReceiver` (`boot/`) catches `BOOT_COMPLETED` and calls `AgentService.start(context)`.
- `AgentService` (`service/`) is a foreground service (`specialUse`, notification ID 69, `START_STICKY`). `dumpsys` shows `isForeground=true`.
- 20 s after the service starts, it calls `AgentAccessibilityService.instance?.launchPackage("com.ghostlight.novaxr")`. The immersive app comes to the front with no operator input. This is the auto-launch POC.
- Manifest: `RECEIVE_BOOT_COMPLETED`, `FOREGROUND_SERVICE`, `FOREGROUND_SERVICE_SPECIAL_USE`, `POST_NOTIFICATIONS`, and `<queries>` for the target package.

Not done yet: no hub connection, no config (package and delay are hardcoded), nothing re-launches the app if it exits, and the headset's 2D "home" is still what shows between boot and the launch.

## Open questions (ask when the stage arrives)

| # | Question | Stage | Status |
|---|----------|-------|--------|
| Q2 | Foreground-app detection. Accessibility window events are push-based and cheap, so usage-stats polling (5s) looks redundant. UNKNOWN: do VR apps emit window events on Quest? Test: log `event.packageName` while launching a VR app, going home, launching a 2D app. If VR is covered, drop usage stats + `PACKAGE_USAGE_STATS`. | 4-5 | Open, experiment planned (E2) |
| Q3 | Config delivery: adb broadcast today (needs PC). Should config come from the hub so auto-launch app can change remotely? | 6 | Open |
| Q4 | Boot ordering: when is Quest ready to launch a VR app after BOOT_COMPLETED? (wait for Wi-Fi / shell) | 3 | Partly answered: a fixed 20 s delay worked once (service started ~13:07:02, launch at 13:07:22, shell activities were still starting at 13:07:06). One run proves little. Test shorter/longer delays and a slow boot, then replace the timer with a signal |
| Q5 | Does Quest kill the foreground service in sleep/standby? Wake lock / keepalive needed? | 3 | Open |
| Q6 | Release signing/`testOnly`: production signing and a Linux-friendly keystore location. Dev side solved by D10 | later | Open (release only) |
| Q7 | "Keep it in place": how to enforce the default app (relaunch on exit? only when idle? operator override?) | 6 | Open |
| Q8 | minSdk: template says 34, old agent 29. Test headset reports SDK 34. Is the whole fleet on 34+? Lower minSdk only if older headsets exist | 1 | Open |
| Q11 | Receivers/service are exported + DUMP-protected so adb can drive them. Keep that, or deliver config another way? | 2 | Open |
| Q12 | Wireless debugging keeper. Facts (E1): Android turns it off at boot and on any Wi-Fi drop; trust (Always allow) survives Wi-Fi drops but NOT a reboot, so one operator tap per reboot is unavoidable; auto-click failed. Decision needed: build the keeper (re-enable after Wi-Fi returns, tap after reboot), or drop wireless adb and rely on the hub + USB for maintenance (E1c). Defer until stage 4+; not needed for the core agent. | 3-6 | Open, deferred |
| Q13 | Reaching the accessibility service from other code (launch app, press home): old agent used a static `Instance`. Alternatives that avoid global state? | 5 | Open. POC uses a `@Volatile` static `instance` (D12). Known gap: it is null if the service is not connected yet or was turned off, and the launch is then silently skipped (`launch result: null`) with no retry |
| Q14 | Will the accessibility launch path survive a Horizon OS update? The log says "With BAL hardening this activity start would be blocked!" (see Findings) | 3-5 | Open, re-test after each OS update. Fallbacks: vrshell broadcast launch, or MDM launcher |
| Q15 | `START_STICKY` plus the launch in `onStartCommand` means every service restart re-launches the app, even if an operator closed it on purpose | 6 | Open, overlaps Q7. Decide before the service gets more logic |

## Backlog ideas (not scheduled)

- In-headset debug panel, like Meta's 2D settings panel: tabs for live status, settings, log viewer, and a way to toggle settings / send commands manually. Build once the agent core works.

## Experiments (planned)

**E1: why does the wireless-debugging prompt keep coming back?** Change one thing at a time.
1. Baseline: headset on Wi-Fi, record SSID + BSSID (`dumpsys wifi`), enable wireless debugging, note whether the prompt appears.
2. Tick "Always allow on this network", reboot, enable again. Prompt back? Same BSSID?
3. Walk to another access point in the venue (same SSID). BSSID changed? Prompt back?
4. Location: with and without the location grant, does the BSSID read as null?
5. Check Wi-Fi MAC randomization setting for the SSID.
Outcome decides: fix trust (stable BSSID / location grant), or accept the prompt and leave it to the operator.
E1b (next): does Always allow survive a Wi-Fi off/on (no reboot)? If yes, only reboot clears it.
E1c: is wireless adb after reboot needed at all? List what actually needs adb (APK install/update, logs) vs what the SignalR hub already covers (Launch, Home, Lock).

**E2: do VR apps trigger accessibility window events?** See Q2.

## Findings (observed on the test headset, Quest, Android SDK 34)

- No accessibility service is enabled by default: `enabled_accessibility_services` is `null`, `accessibility_enabled` is `0`. Provisioning must still read-then-append, other headsets may differ.
- Enabling only `enabled_accessibility_services` (with `accessibility_enabled` still `0`) was enough for Android to bind the service. Do not rely on that: provisioning should set both.
- To undo, use `settings delete secure enabled_accessibility_services`. `put ... ""` loses its quotes through the PC shell and does nothing.
- `AuthenticatorException` from `com.oculus.horizon` in logcat is Meta account-sync noise, unrelated to the agent.
- Baseline before E1: `adb_enabled`=1, `adb_wifi_enabled`=0, headset reported Wi-Fi DISCONNECTED (BSSID none) while attached over USB.
- E1 step 1 baseline: SSID "GLS", BSSID 94:83:c4:89:e3:21, headset IP 192.168.8.223, Wi-Fi MAC c0:dd:8a:a5:ce:dd (not a locally-administered 02:... address, so randomization looks off for this network). No internet on this network. The app has no location permission yet (not declared in the manifest).
- Lab network: headset joins the GLS router (LAN with the PC via ethernet, no internet); the PC also uses Wi-Fi on a separate internet router (192.168.1.x). Mimics a venue: headset network has no internet.
- E1 results so far (BSSID 94:83:c4:89:e3:21 throughout): enabling `adb_wifi_enabled` from the shell with the BSSID already known still shows the trust prompt, so the "enable before BSSID is known" theory in the old keeper comment is not the cause. Plain Allow did not flip the setting off, but toggling off/on again with the same BSSID showed the prompt again, so plain Allow is not remembered. Still to test: "Always allow" (E1 step 2), then across a reboot.
- `adb mdns services` kept listing the headset at 192.168.1.69:46445 (stale, other subnet) while the headset is 192.168.8.223, even after minutes. mDNS discovery from a PC with two networks may be unreliable. Matters for how the PC/hub finds a headset after the random port changes.
- E1 step 2 (Always allow ticked, setting toggled off/on by `settings put global adb_wifi_enabled`): no prompt on later toggles in the same boot. The prompt is triggered by enabling the setting alone, no PC connect needed.
- E1 reboot test (USB attached, setting `1` before reboot): setting read `0` after boot. Logcat at boot: `AdbDebuggingManager: Not connected to any wireless network. Not enabling adbwifi.` So Android refuses to enable it before Wi-Fi is up, matching the old keeper's wait-for-Wi-Fi logic. After Wi-Fi was up, enabling again (BSSID unchanged, 94:83:c4:89:e3:21) showed the trust prompt AGAIN despite the earlier Always allow. Conclusion so far: "Always allow" does not survive a reboot on this Quest, and a missing/changing BSSID is NOT the reason (BSSID stable). Cause inside Quest's trust storage is unknown (logcat showed no trust-decision lines).
- mDNS: each enable picks a new random port (46445, 44095, 37265, 34801 seen). The PC's mDNS list also kept old records, and showed one at 192.168.1.69 (not a headset address on GLS). Do not trust cached mDNS entries; confirm with `adbd: adbwifi started on port N` in logcat.
- E1b: with Always allow ticked, trust survived turning Wi-Fi off/on and toggling wireless debugging off/on (no prompt). Only a reboot cleared it (prompt returned after reboot even though Always allow had been ticked). Turning Wi-Fi off also switches wireless debugging off, and it stays off when Wi-Fi returns, so re-enabling is needed after any Wi-Fi drop, not only at boot.
- E1 conclusion: not the BSSID, not the port. The trust prompt is once per boot per network at best. Auto-click on Meta's VR-shell dialog did not work earlier, so a headset that reboots needs one operator tap before wireless adb works.
- ROOT-CAUSE LEADS (read-only): (1) `adb shell dumpsys adb` prints Android's adb keystore, including `<wifiAP bssid="94:83:c4:89:e3:21" />` after Always allow, so the trusted-network list is a real stored entry (keyed by BSSID). (2) The prompt is a normal Android activity: `com.oculus.os.vrusb/.WifiDebuggingAlertActivity` (Meta system app), so accessibility might see it; why the old auto-click failed is still unknown. (3) logcat confirms `AdbDebuggingManager: Wifi disabled. Disabling adbwifi.` on Wi-Fi off. NEXT TEST (E1d): reboot, and BEFORE enabling wireless debugging run `dumpsys adb`. If the wifiAP entry is gone, the store is cleared on reboot. If it is still there but the prompt appears anyway, Meta's vrusb prompt logic is not using it.
- Debug builds carry `TEST_ONLY` (from `src/debug/AndroidManifest.xml`), so `dpm remove-active-admin` works on them.

- Boot receivers never fire for a *stopped* package. After `adb install` (or an Android Studio redeploy) the package is `stopped=true` (`dumpsys package <pkg> | grep stopped`) and `BOOT_COMPLETED` skips it, silently. Fix: run `adb shell am start -n com.blendedxr.gls.agent/.MainActivity` once. The provisioning script must do this after installing.
- Android Studio's Logcat only shows lines after it (re)attaches, so boot-time logs from a reboot are missed there, and `package:mine` can hide early lines. Filter with `tag:MrCosmo`, or test the same code while attached. On the command line, `adb logcat -s MrCosmo:Boot` does NOT work because the colon in the tag breaks `tag:priority` syntax; use `grep MrCosmo`. A tag without a colon would avoid this.
- Launch from the accessibility service was allowed: `START ... from uid 10018 (BAL_ALLOW_PERMISSION) result code=0`, then a second start `(realCallingUid=1000) (BAL_ALLOW_PENDING_INTENT)`. The log shows `originatingPendingIntent: ... com.blendedxr.gls.agent startActivity` with `realCallingPackage: android.uid.system:1000`. This suggests (not confirmed) that the accessibility framework sends the `startActivity` from the system process using a PendingIntent, and that is what the BAL check accepts. Our own process was only `FOREGROUND_SERVICE` with no visible window, which would normally be blocked.
- The same log line has an error-level warning: `With BAL hardening this activity start would be blocked! [... callingUidHasAnyVisibleWindow: false ... callingUidProcState: FOREGROUND_SERVICE ...]`. It is a warning today (the launch went through). It means the allow is on a rule Android is planning to tighten, so a future Horizon OS update could break auto-launch (Q14). Re-test after OS updates.
- The immersive app took over cleanly (`UiModeController: Switching from com.oculus.vrshell to com.ghostlight.novaxr`, `IMMERSIVE`). That answers the old "does `startActivity` foreground a VR app on Horizon OS" hardware question for this app, via the accessibility path.
- `FOREGROUND_SERVICE` start from `BOOT_COMPLETED` is allowed through a 20 s temp allow-list (`tempAllowListReason ... BOOT_COMPLETED, duration:20000`). Anything the service does that needs that allowance must happen in the first 20 s.

## Closed questions

| # | Question | Answer |
|---|----------|--------|
| Q1 | Accessibility vs exact alarm / SYSTEM_ALERT_WINDOW | Keep accessibility, no gestures (D9) |
| Q9 | UI toolkit for the status screen | Plain Views (D8) |
| Q10 | Where does the repo live? | Own private repo for now, merged into Blended-XP later (D7) |
