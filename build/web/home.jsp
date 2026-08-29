<%@page import="java.util.Map"%>
<%@page import="java.util.List"%>
<%@page contentType="text/html" pageEncoding="UTF-8"%>

<%
    List<Map<String, String>> dentists =
            (List<Map<String, String>>) request.getAttribute("dentists");
    Boolean clinicOpen = (Boolean) request.getAttribute("clinicOpen");
    Boolean scheduleUnavailable =
            (Boolean) request.getAttribute("scheduleUnavailable");
%>

<!DOCTYPE html>
<html lang="en">
<head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1.0">
    <meta name="description"
          content="Sunrise Dental Clinic information, announcements, news and today's dentist visiting hours.">
    <title>Sunrise Dental Clinic</title>

    <style>
        :root {
            --navy: #123047;
            --blue: #176b87;
            --teal: #21a7a0;
            --mint: #e9f8f5;
            --cream: #fbfaf6;
            --white: #ffffff;
            --text: #173042;
            --muted: #607583;
            --line: #dce9e8;
            --coral: #ed7f65;
        }

        * { box-sizing: border-box; }

        html { scroll-behavior: smooth; }

        body {
            margin: 0;
            color: var(--text);
            background: var(--cream);
            font-family: Arial, Helvetica, sans-serif;
            line-height: 1.6;
        }

        a { color: inherit; }

        .container {
            width: min(1160px, calc(100% - 40px));
            margin: 0 auto;
        }

        .topbar {
            padding: 8px 0;
            color: #d7edee;
            background: var(--navy);
            font-size: 13px;
        }

        .topbar-inner {
            display: flex;
            justify-content: space-between;
            gap: 20px;
        }

        .navbar {
            position: sticky;
            top: 0;
            z-index: 20;
            background: rgba(255, 255, 255, 0.96);
            border-bottom: 1px solid rgba(23, 107, 135, 0.12);
            backdrop-filter: blur(10px);
        }

        .nav-inner {
            min-height: 76px;
            display: flex;
            align-items: center;
            justify-content: space-between;
            gap: 28px;
        }

        .brand {
            display: flex;
            align-items: center;
            gap: 12px;
            text-decoration: none;
        }

        .brand-mark {
            width: 44px;
            height: 44px;
            display: grid;
            place-items: center;
            color: white;
            background: linear-gradient(145deg, var(--teal), var(--blue));
            border-radius: 14px 14px 20px 20px;
            font-size: 25px;
            font-weight: bold;
            box-shadow: 0 8px 22px rgba(23, 107, 135, 0.22);
        }

        .brand-name { display: block; font-size: 18px; font-weight: 800; }
        .brand-tagline { display: block; color: var(--muted); font-size: 11px; letter-spacing: .12em; text-transform: uppercase; }

        .nav-links { display: flex; align-items: center; gap: 27px; }
        .nav-links a { text-decoration: none; font-size: 14px; font-weight: 700; }
        .nav-links a:hover { color: var(--teal); }

        .button {
            display: inline-flex;
            align-items: center;
            justify-content: center;
            min-height: 46px;
            padding: 0 23px;
            border: 0;
            border-radius: 10px;
            color: white;
            background: var(--blue);
            text-decoration: none;
            font-size: 14px;
            font-weight: 800;
            box-shadow: 0 9px 22px rgba(23, 107, 135, 0.2);
            transition: transform .2s, box-shadow .2s;
        }

        .button:hover { transform: translateY(-2px); box-shadow: 0 12px 27px rgba(23, 107, 135, .28); }
        .button.secondary { color: var(--blue); background: white; border: 1px solid #c9dddd; box-shadow: none; }

        .hero {
            overflow: hidden;
            background:
                radial-gradient(circle at 87% 22%, rgba(33,167,160,.18), transparent 27%),
                linear-gradient(120deg, #f4fbf9 0%, #fff 55%, #e8f6f5 100%);
        }

        .hero-grid {
            min-height: 590px;
            display: grid;
            grid-template-columns: 1.08fr .92fr;
            align-items: center;
            gap: 65px;
            padding: 70px 0;
        }

        .eyebrow {
            display: inline-flex;
            align-items: center;
            gap: 9px;
            padding: 7px 13px;
            border-radius: 999px;
            color: var(--blue);
            background: var(--mint);
            font-size: 12px;
            font-weight: 800;
            letter-spacing: .08em;
            text-transform: uppercase;
        }

        .eyebrow::before { content: ""; width: 7px; height: 7px; border-radius: 50%; background: var(--teal); }

        h1 {
            max-width: 650px;
            margin: 20px 0 20px;
            color: var(--navy);
            font-family: Georgia, 'Times New Roman', serif;
            font-size: clamp(43px, 6vw, 73px);
            line-height: 1.05;
            letter-spacing: -.035em;
        }

        h1 span { color: var(--teal); }

        .hero-copy > p {
            max-width: 590px;
            margin: 0 0 30px;
            color: var(--muted);
            font-size: 18px;
        }

        .hero-actions { display: flex; flex-wrap: wrap; gap: 13px; }

        .hero-visual {
            position: relative;
            min-height: 430px;
            display: grid;
            place-items: center;
        }

        .visual-circle {
            width: min(390px, 90vw);
            aspect-ratio: 1;
            display: grid;
            place-items: center;
            border-radius: 50%;
            background: linear-gradient(145deg, #daf3ef, #8ed4ce);
            box-shadow: inset 0 0 0 28px rgba(255,255,255,.35), 0 30px 60px rgba(18,48,71,.16);
        }

        .tooth {
            color: white;
            font-family: Georgia, serif;
            font-size: 155px;
            filter: drop-shadow(0 13px 14px rgba(23,107,135,.16));
        }

        .open-card {
            position: absolute;
            left: 0;
            bottom: 35px;
            width: 245px;
            padding: 18px;
            border-radius: 14px;
            background: white;
            box-shadow: 0 18px 45px rgba(18,48,71,.16);
        }

        .open-card small { color: var(--muted); }
        .open-card strong { display: block; margin-top: 3px; color: var(--navy); }
        .status { display: inline-block; width: 9px; height: 9px; margin-right: 7px; border-radius: 50%; background: #2dbb75; }
        .status.closed { background: var(--coral); }

        section { padding: 82px 0; }
        .section-soft { background: #eef8f6; }

        .section-head { max-width: 720px; margin-bottom: 35px; }
        .section-head h2 { margin: 11px 0; color: var(--navy); font-family: Georgia, serif; font-size: clamp(31px, 4vw, 45px); line-height: 1.15; }
        .section-head p { margin: 0; color: var(--muted); }

        .about-grid { display: grid; grid-template-columns: .85fr 1.15fr; gap: 55px; align-items: start; }
        .about-panel { padding: 34px; color: white; background: var(--navy); border-radius: 22px; }
        .about-panel h3 { margin: 0 0 10px; font-family: Georgia, serif; font-size: 28px; }
        .about-panel p { color: #c9dde3; }
        .hours-list { margin-top: 24px; border-top: 1px solid rgba(255,255,255,.15); }
        .hours-row { display: flex; justify-content: space-between; gap: 15px; padding: 12px 0; border-bottom: 1px solid rgba(255,255,255,.12); font-size: 14px; }

        .feature-grid { display: grid; grid-template-columns: repeat(2, 1fr); gap: 18px; }
        .feature { padding: 24px; background: white; border: 1px solid var(--line); border-radius: 16px; }
        .feature-number { color: var(--teal); font-family: Georgia, serif; font-size: 28px; font-weight: bold; }
        .feature h3 { margin: 7px 0; color: var(--navy); font-size: 17px; }
        .feature p { margin: 0; color: var(--muted); font-size: 14px; }

        .schedule-wrap { display: grid; grid-template-columns: 1fr 2fr; gap: 25px; }
        .today-card { padding: 30px; border-radius: 20px; color: white; background: linear-gradient(145deg, var(--blue), var(--navy)); }
        .today-card .date { color: #cce8e8; font-size: 14px; }
        .today-card h3 { margin: 16px 0 4px; font-family: Georgia, serif; font-size: 29px; }
        .today-card p { margin: 0; color: #d5e9ed; }

        .doctor-list { display: grid; gap: 12px; }
        .doctor-card { display: grid; grid-template-columns: 46px 1fr auto; align-items: center; gap: 16px; padding: 18px 21px; background: white; border: 1px solid var(--line); border-radius: 14px; }
        .doctor-avatar { width: 46px; height: 46px; display: grid; place-items: center; color: var(--blue); background: var(--mint); border-radius: 50%; font-family: Georgia, serif; font-weight: bold; }
        .doctor-card h3 { margin: 0; color: var(--navy); font-size: 16px; }
        .doctor-card p { margin: 1px 0 0; color: var(--muted); font-size: 13px; }
        .doctor-time { color: var(--blue); font-size: 13px; font-weight: 800; text-align: right; }
        .empty-card { padding: 25px; text-align: center; background: white; border: 1px dashed #aecac8; border-radius: 14px; color: var(--muted); }

        .notice-grid { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
        .notice-card { position: relative; padding: 27px; overflow: hidden; background: white; border: 1px solid var(--line); border-radius: 17px; }
        .notice-card::before { content: ""; position: absolute; inset: 0 auto 0 0; width: 4px; background: var(--teal); }
        .notice-type { color: var(--coral); font-size: 11px; font-weight: 800; letter-spacing: .1em; text-transform: uppercase; }
        .notice-card h3 { margin: 9px 0; color: var(--navy); font-size: 18px; }
        .notice-card p { margin: 0; color: var(--muted); font-size: 14px; }

        .milestones { display: grid; grid-template-columns: repeat(3, 1fr); gap: 20px; }
        .milestone { padding: 27px; border-radius: 17px; background: #f8fcfb; border: 1px solid var(--line); }
        .milestone-label { color: var(--teal); font-size: 12px; font-weight: 800; text-transform: uppercase; letter-spacing: .08em; }
        .milestone h3 { margin: 9px 0; color: var(--navy); }
        .milestone p { margin: 0; color: var(--muted); font-size: 14px; }

        .cta { padding: 55px 0; background: var(--navy); color: white; }
        .cta-inner { display: flex; justify-content: space-between; align-items: center; gap: 35px; }
        .cta h2 { margin: 0 0 7px; font-family: Georgia, serif; font-size: 34px; }
        .cta p { margin: 0; color: #cce0e6; }

        footer { padding: 32px 0; background: #0c2638; color: #bcd0d7; font-size: 13px; }
        .footer-inner { display: flex; justify-content: space-between; gap: 25px; }

        @media (max-width: 900px) {
            .nav-links a:not(.button) { display: none; }
            .hero-grid, .about-grid, .schedule-wrap { grid-template-columns: 1fr; }
            .hero-grid { padding: 55px 0; }
            .hero-visual { min-height: 390px; }
            .notice-grid, .milestones { grid-template-columns: 1fr 1fr; }
        }

        @media (max-width: 620px) {
            .container { width: min(100% - 28px, 1160px); }
            .topbar-inner { justify-content: center; text-align: center; }
            .topbar-inner span:last-child { display: none; }
            .nav-inner { min-height: 68px; }
            .brand-tagline { display: none; }
            .nav-links { gap: 0; }
            .nav-links .button { min-height: 40px; padding: 0 16px; }
            .hero-grid { min-height: auto; gap: 30px; padding: 45px 0 60px; }
            .hero-visual { display: none; }
            section { padding: 60px 0; }
            .feature-grid, .notice-grid, .milestones { grid-template-columns: 1fr; }
            .doctor-card { grid-template-columns: 42px 1fr; }
            .doctor-time { grid-column: 2; text-align: left; }
            .cta-inner, .footer-inner { align-items: flex-start; flex-direction: column; }
        }
    </style>
</head>

<body>
    <div class="topbar">
        <div class="container topbar-inner">
            <span>Compassionate dental care for every smile</span>
            <span>Appointments are subject to doctor confirmation</span>
        </div>
    </div>

    <header class="navbar">
        <div class="container nav-inner">
            <a class="brand" href="${pageContext.request.contextPath}/HomeServlet">
                <span class="brand-mark" aria-hidden="true">S</span>
                <span>
                    <span class="brand-name">Sunrise Dental Clinic</span>
                    <span class="brand-tagline">Care · Comfort · Confidence</span>
                </span>
            </a>

            <nav class="nav-links" aria-label="Main navigation">
                <a href="#about">About</a>
                <a href="#visiting-hours">Visiting hours</a>
                <a href="#announcements">Announcements</a>
                <a class="button" href="${pageContext.request.contextPath}/login.jsp">Login</a>
            </nav>
        </div>
    </header>

    <main>
        <section class="hero">
            <div class="container hero-grid">
                <div class="hero-copy">
                    <span class="eyebrow">Welcome to Sunrise</span>
                    <h1>Healthy smiles begin with <span>thoughtful care.</span></h1>
                    <p>
                        A welcoming dental clinic focused on accessible, coordinated
                        and patient-centred oral healthcare for the whole family.
                    </p>
                    <div class="hero-actions">
                        <a class="button" href="${pageContext.request.contextPath}/login.jsp">Login to your account</a>
                        <a class="button secondary" href="#visiting-hours">Today's doctors</a>
                    </div>
                </div>

                <div class="hero-visual" aria-hidden="true">
                    <div class="visual-circle"><span class="tooth">♢</span></div>
                    <div class="open-card">
                        <small><span class="status <%= Boolean.TRUE.equals(clinicOpen) ? "" : "closed" %>"></span>
                            <%= Boolean.TRUE.equals(clinicOpen) ? "Clinic open today" : "Clinic closed today" %>
                        </small>
                        <strong><%= request.getAttribute("visitingHours") %></strong>
                    </div>
                </div>
            </div>
        </section>

        <section id="about">
            <div class="container about-grid">
                <div class="about-panel">
                    <h3>Clinic hours</h3>
                    <p>Plan your visit during our regular opening hours.</p>
                    <div class="hours-list">
                        <div class="hours-row"><span>Monday – Friday</span><strong>9:00 AM – 7:00 PM</strong></div>
                        <div class="hours-row"><span>Saturday</span><strong>8:30 AM – 1:00 PM</strong></div>
                        <div class="hours-row"><span>Sunday</span><strong>Closed</strong></div>
                    </div>
                </div>

                <div>
                    <div class="section-head">
                        <span class="eyebrow">About our clinic</span>
                        <h2>Care designed around our patients</h2>
                        <p>
                            Sunrise Dental Clinic brings patients, dentists and clinic staff
                            together through a clear appointment process and attentive care.
                        </p>
                    </div>
                    <div class="feature-grid">
                        <article class="feature"><span class="feature-number">01</span><h3>Qualified dental team</h3><p>Choose a dentist according to the treatment you need.</p></article>
                        <article class="feature"><span class="feature-number">02</span><h3>Simple appointments</h3><p>Registered patients can request and follow appointments online.</p></article>
                        <article class="feature"><span class="feature-number">03</span><h3>Coordinated care</h3><p>Clinic staff and dentists review each request before confirmation.</p></article>
                        <article class="feature"><span class="feature-number">04</span><h3>Clear billing</h3><p>Treatment charges and consultation fees are presented together.</p></article>
                    </div>
                </div>
            </div>
        </section>

        <section id="visiting-hours" class="section-soft">
            <div class="container">
                <div class="section-head">
                    <span class="eyebrow">Plan your visit</span>
                    <h2>Doctor visiting hours for today</h2>
                    <p>Doctor availability may change if an emergency occurs. Please confirm your appointment after logging in.</p>
                </div>

                <div class="schedule-wrap">
                    <aside class="today-card">
                        <span class="date"><%= request.getAttribute("today") %></span>
                        <h3><%= Boolean.TRUE.equals(clinicOpen) ? "We're open" : "We're closed" %></h3>
                        <p><%= request.getAttribute("visitingHours") %></p>
                    </aside>

                    <div class="doctor-list">
                        <% if (dentists != null && !dentists.isEmpty() && Boolean.TRUE.equals(clinicOpen)) {
                            for (Map<String, String> dentist : dentists) { %>
                                <article class="doctor-card">
                                    <div class="doctor-avatar" aria-hidden="true">Dr</div>
                                    <div>
                                        <h3><%= dentist.get("name") %></h3>
                                        <p><%= dentist.get("specialization") %></p>
                                    </div>
                                    <div class="doctor-time"><%= dentist.get("visitingHours") %></div>
                                </article>
                        <%  }
                           } else { %>
                            <div class="empty-card">
                                <% if (Boolean.TRUE.equals(scheduleUnavailable)) { %>
                                    Today's doctor schedule is temporarily unavailable. Please check again shortly.
                                <% } else if (!Boolean.TRUE.equals(clinicOpen)) { %>
                                    There are no regular doctor visits today. The clinic reopens on Monday.
                                <% } else { %>
                                    No doctor visits are listed for today. Please contact the clinic before travelling.
                                <% } %>
                            </div>
                        <% } %>
                    </div>
                </div>
            </div>
        </section>

        <section id="announcements">
            <div class="container">
                <div class="section-head">
                    <span class="eyebrow">Notice board</span>
                    <h2>Clinic announcements & news</h2>
                    <p>Useful updates for patients and clinic visitors.</p>
                </div>
                <div class="notice-grid">
                    <article class="notice-card"><span class="notice-type">Announcement</span><h3>Online appointment requests</h3><p>Patient account holders can now select a treatment and request an appointment with an available dentist.</p></article>
                    <article class="notice-card"><span class="notice-type">Patient notice</span><h3>Arrive a little early</h3><p>Please arrive 10 minutes before your confirmed appointment to allow time for reception and preparation.</p></article>
                    <article class="notice-card"><span class="notice-type">Clinic news</span><h3>Follow your appointment</h3><p>Patients can sign in to view their appointment number and latest confirmation status.</p></article>
                </div>
            </div>
        </section>

        <section class="section-soft">
            <div class="container">
                <div class="section-head">
                    <span class="eyebrow">Our progress</span>
                    <h2>Clinic achievements</h2>
                    <p>Milestones that help us offer a smoother patient experience.</p>
                </div>
                <div class="milestones">
                    <article class="milestone"><span class="milestone-label">Digital access</span><h3>Online patient services</h3><p>Introduced account registration, appointment requests and status tracking in one place.</p></article>
                    <article class="milestone"><span class="milestone-label">Better coordination</span><h3>Connected care workflow</h3><p>Created a coordinated appointment process for patients, administrators and dentists.</p></article>
                    <article class="milestone"><span class="milestone-label">Clear service</span><h3>Transparent treatment billing</h3><p>Made treatment cost, consultation fees and total billing information easier to understand.</p></article>
                </div>
            </div>
        </section>
    </main>

    <section class="cta">
        <div class="container cta-inner">
            <div><h2>Ready to manage your dental care?</h2><p>Login to request an appointment or access your clinic dashboard.</p></div>
            <a class="button" href="${pageContext.request.contextPath}/login.jsp">Continue to login</a>
        </div>
    </section>

    <footer>
        <div class="container footer-inner">
            <span>© <%= java.time.Year.now() %> Sunrise Dental Clinic. All rights reserved.</span>
            <span>Care · Comfort · Confidence</span>
        </div>
    </footer>
</body>
</html>
