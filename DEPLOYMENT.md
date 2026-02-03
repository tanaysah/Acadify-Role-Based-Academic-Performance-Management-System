# 🚀 Deployment Guide

Complete guide for deploying acadify frontend to production.

---

## 📋 Pre-Deployment Checklist

- [ ] Backend API is deployed and accessible
- [ ] Database is set up and populated
- [ ] Python analytics service is running
- [ ] Environment variables are configured
- [ ] Build passes locally (`npm run build`)
- [ ] All features tested in production mode

---

## 🌐 Deployment Options

### Option 1: Vercel (Recommended)

**Why Vercel?**
- ✅ Zero configuration
- ✅ Automatic HTTPS
- ✅ Global CDN
- ✅ Free tier available
- ✅ Perfect for React/Vite apps

**Steps:**

1. **Install Vercel CLI**
```bash
npm install -g vercel
```

2. **Login to Vercel**
```bash
vercel login
```

3. **Deploy**
```bash
vercel
```

4. **Set Environment Variables**
```bash
vercel env add VITE_API_URL production
# Enter: https://api.acadify.com/api
```

5. **Deploy to Production**
```bash
vercel --prod
```

**Custom Domain:**
```bash
vercel domains add acadify.com
```

---

### Option 2: Netlify

**Steps:**

1. **Install Netlify CLI**
```bash
npm install -g netlify-cli
```

2. **Build the project**
```bash
npm run build
```

3. **Deploy**
```bash
netlify deploy --prod --dir=dist
```

4. **Configure Environment Variables**
- Go to Site settings → Environment variables
- Add `VITE_API_URL = https://api.acadify.com/api`

**netlify.toml** (create in root):
```toml
[build]
  command = "npm run build"
  publish = "dist"

[[redirects]]
  from = "/*"
  to = "/index.html"
  status = 200
```

---

### Option 3: AWS S3 + CloudFront

**Why AWS?**
- ✅ Complete control
- ✅ Highly scalable
- ✅ Can integrate with other AWS services

**Steps:**

1. **Build the project**
```bash
npm run build
```

2. **Create S3 Bucket**
```bash
aws s3 mb s3://acadify-frontend
```

3. **Configure bucket for static hosting**
```bash
aws s3 website s3://acadify-frontend \
  --index-document index.html \
  --error-document index.html
```

4. **Upload build files**
```bash
aws s3 sync dist/ s3://acadify-frontend
```

5. **Make bucket public**
```json
{
  "Version": "2012-10-17",
  "Statement": [
    {
      "Sid": "PublicReadGetObject",
      "Effect": "Allow",
      "Principal": "*",
      "Action": "s3:GetObject",
      "Resource": "arn:aws:s3:::acadify-frontend/*"
    }
  ]
}
```

6. **Set up CloudFront (CDN)**
- Create CloudFront distribution
- Point to S3 bucket
- Configure custom domain
- Enable HTTPS

---

### Option 4: Docker + Any Cloud

**Dockerfile:**
```dockerfile
# Build stage
FROM node:18-alpine AS build

WORKDIR /app
COPY package*.json ./
RUN npm ci
COPY . .
RUN npm run build

# Production stage
FROM nginx:alpine

COPY --from=build /app/dist /usr/share/nginx/html
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
```

**nginx.conf:**
```nginx
server {
    listen 80;
    server_name _;
    root /usr/share/nginx/html;
    index index.html;

    location / {
        try_files $uri $uri/ /index.html;
    }

    # Cache static assets
    location ~* \.(js|css|png|jpg|jpeg|gif|ico|svg)$ {
        expires 1y;
        add_header Cache-Control "public, immutable";
    }
}
```

**Build and run:**
```bash
docker build -t acadify-frontend .
docker run -p 80:80 acadify-frontend
```

**Deploy to:**
- AWS ECS
- Google Cloud Run
- DigitalOcean App Platform
- Render

---

## 🔐 Environment Variables

### Production Environment File

Create `.env.production`:
```env
# Production Backend API
VITE_API_URL=https://api.acadify.com/api

# Optional: Analytics
VITE_ANALYTICS_ID=UA-XXXXXXXXX-X
```

### Setting Environment Variables by Platform

**Vercel:**
```bash
vercel env add VITE_API_URL production
```

**Netlify:**
Site Settings → Environment Variables → New Variable

**AWS/Docker:**
Set in container environment or EC2 user data

---

## 🔗 CORS Configuration

**Backend must allow frontend domain:**

```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                .allowedOrigins(
                    "https://acadify.com",
                    "https://www.acadify.com"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

---

## 🌍 Custom Domain Setup

### DNS Configuration

Add these DNS records:

```
Type    Name    Value
A       @       <Vercel/Netlify IP>
CNAME   www     <your-app>.vercel.app
```

### SSL/HTTPS

- **Vercel/Netlify**: Automatic SSL (Let's Encrypt)
- **CloudFront**: Free SSL certificate via AWS Certificate Manager
- **Manual**: Use Let's Encrypt + Certbot

---

## 📊 Performance Optimization

### 1. Build Optimization

**vite.config.js:**
```javascript
export default defineConfig({
  build: {
    rollupOptions: {
      output: {
        manualChunks: {
          'vendor': ['react', 'react-dom', 'react-router-dom'],
          'three': ['three', '@react-three/fiber', '@react-three/drei'],
          'motion': ['framer-motion'],
        }
      }
    },
    minify: 'terser',
    terserOptions: {
      compress: {
        drop_console: true,  // Remove console.logs
      }
    }
  }
});
```

### 2. CDN Configuration

**CloudFront Cache Behaviors:**
- `*.js, *.css`: Cache for 1 year
- `index.html`: No cache (always fresh)
- Images: Cache for 1 month

### 3. Lazy Loading

Routes are already lazy-loaded in the starter code.

---

## 🧪 Testing Production Build

### Local Testing

```bash
# Build
npm run build

# Preview
npm run preview
```

### Production Checklist

- [ ] All pages load correctly
- [ ] Login/logout works
- [ ] Session persists across page refresh
- [ ] API calls succeed
- [ ] 3D canvas renders smoothly
- [ ] Responsive on mobile
- [ ] HTTPS is enforced
- [ ] Console has no errors

---

## 📈 Monitoring & Analytics

### Add Google Analytics (Optional)

**Install:**
```bash
npm install react-ga4
```

**Setup in main.jsx:**
```javascript
import ReactGA from 'react-ga4';

ReactGA.initialize('G-XXXXXXXXXX');

// Track page views
import { useLocation } from 'react-router-dom';
import { useEffect } from 'react';

function Analytics() {
  const location = useLocation();
  
  useEffect(() => {
    ReactGA.send({ hitType: 'pageview', page: location.pathname });
  }, [location]);
  
  return null;
}
```

### Error Tracking

Consider adding:
- Sentry (error tracking)
- LogRocket (session replay)
- Datadog (performance monitoring)

---

## 🔄 CI/CD Pipeline

### GitHub Actions

**.github/workflows/deploy.yml:**
```yaml
name: Deploy to Production

on:
  push:
    branches: [main]

jobs:
  deploy:
    runs-on: ubuntu-latest
    
    steps:
      - uses: actions/checkout@v3
      
      - name: Setup Node.js
        uses: actions/setup-node@v3
        with:
          node-version: '18'
      
      - name: Install dependencies
        run: npm ci
      
      - name: Build
        run: npm run build
        env:
          VITE_API_URL: ${{ secrets.VITE_API_URL }}
      
      - name: Deploy to Vercel
        uses: amondnet/vercel-action@v25
        with:
          vercel-token: ${{ secrets.VERCEL_TOKEN }}
          vercel-org-id: ${{ secrets.ORG_ID }}
          vercel-project-id: ${{ secrets.PROJECT_ID }}
          vercel-args: '--prod'
```

---

## 🚨 Rollback Strategy

### Vercel/Netlify
- Both platforms keep deployment history
- Rollback from dashboard or CLI

```bash
# Vercel
vercel rollback <deployment-url>

# Netlify
netlify deploy --alias previous-version
```

### Docker
```bash
# Tag versions
docker tag acadify-frontend:latest acadify-frontend:v1.0.0

# Rollback
docker pull acadify-frontend:v1.0.0
docker run -p 80:80 acadify-frontend:v1.0.0
```

---

## 📦 Production Architecture

```
┌─────────────────────────────────────┐
│         CloudFlare (Optional)       │
│         - DDoS Protection           │
│         - CDN                       │
└──────────────┬──────────────────────┘
               │
               ▼
┌─────────────────────────────────────┐
│      Frontend (Vercel/Netlify)      │
│      - Static React App             │
│      - HTTPS Enforced               │
└──────────────┬──────────────────────┘
               │ API Calls
               ▼
┌─────────────────────────────────────┐
│      Backend API (AWS/Render)       │
│      - Spring Boot                  │
│      - Session Management           │
└──────────────┬──────────────────────┘
               │
        ┌──────┴──────┬──────────┐
        ▼             ▼          ▼
   ┌────────┐   ┌─────────┐  ┌──────┐
   │Postgres│   │  Redis  │  │Python│
   │   DB   │   │(Session)│  │  AI  │
   └────────┘   └─────────┘  └──────┘
```

---

## 🎯 Post-Deployment

1. **Monitor Performance**
   - Check Lighthouse scores
   - Monitor Core Web Vitals
   - Track error rates

2. **User Testing**
   - Test all user flows
   - Check across devices
   - Verify analytics tracking

3. **Documentation**
   - Update API documentation
   - Document deployment process
   - Create runbook for incidents

---

**Deployment Status:** Ready for production 🚀

**Recommended Stack:**
- Frontend: Vercel
- Backend: Render/Railway
- Database: Managed PostgreSQL (DigitalOcean/AWS RDS)
- Python: Same server as backend or separate
