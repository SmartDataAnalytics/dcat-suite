
reset-frontend:
	rm -rf config/stats.json node_modules package.json package-lock.json webpack.config.js webpack.generated.js
	mvn vaadin:prepare-frontend
	mvn vaadin:build-frontend || true
	# TODO replace ~1.6.0 with 1.6.0 for leaflet
	npm install --force
	mvn vaadin:build-frontend

