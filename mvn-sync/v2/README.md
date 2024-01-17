

```
Failed to watch /home/raven/.m2/repository; upper limit on inotify watches reached!
Please increase the amount of inotify watches allowed per user via `/proc/sys/fs/inotify/max_user_watches'.
```

```bash
echo "1000000" | sudo tee /proc/sys/fs/inotify/max_user_watches
```
