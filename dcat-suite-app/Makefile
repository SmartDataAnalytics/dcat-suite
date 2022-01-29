
reset-frontend:
	rm -rf node_modules package.json package-lock.json webpack.config.js webpack.generated.js
	mvn vaadin:prepare-frontend
	npm install --force
	mvn vaadin:build-frontend

