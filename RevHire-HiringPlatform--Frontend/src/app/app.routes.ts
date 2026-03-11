import { Routes } from '@angular/router';
import { authGuard, guestGuard, authChildGuard } from './core/guards/auth.guard';
import { roleGuard } from './core/guards/role.guard';

export const routes: Routes = [
    { path: '', loadComponent: () => import('./features/home/home').then(m => m.HomeComponent) },
    { path: 'jobs', loadComponent: () => import('./features/job-search/job-search').then(m => m.JobSearchComponent) },
    { path: 'jobs/:id', loadComponent: () => import('./features/job-detail/job-detail').then(m => m.JobDetailComponent) },
    { path: 'companies/:id', canActivate: [authGuard], loadComponent: () => import('./features/company-detail/company-detail').then(m => m.CompanyDetailComponent) },

    // Auth
    { path: 'login', canActivate: [guestGuard], loadComponent: () => import('./features/auth/login/login').then(m => m.LoginComponent) },
    { path: 'admin-login', canActivate: [guestGuard], loadComponent: () => import('./features/admin/login/admin-login').then(m => m.AdminLoginComponent) },
    { path: 'register', canActivate: [guestGuard], loadComponent: () => import('./features/auth/register/register').then(m => m.RegisterComponent) },
    { path: 'reset-password', canActivate: [guestGuard], loadComponent: () => import('./features/auth/reset-password/reset-password').then(m => m.ResetPasswordComponent) },
    { path: 'info', loadComponent: () => import('./features/info/info-pages').then(m => m.InfoPagesComponent) },

    // Seeker Routes
    {
        path: 'seeker',
        canActivate: [authGuard],
        canActivateChild: [authChildGuard],
        children: [
            { path: 'dashboard', loadComponent: () => import('./features/seeker/dashboard/seeker-dashboard').then(m => m.SeekerDashboardComponent) },
            { path: 'profile', loadComponent: () => import('./features/seeker/profile/seeker-profile').then(m => m.SeekerProfileComponent) },
            { path: 'resume-builder', loadComponent: () => import('./features/seeker/resume-builder/resume-builder').then(m => m.ResumeBuilderComponent) },
            { path: 'applications', loadComponent: () => import('./features/seeker/my-applications/my-applications').then(m => m.MyApplicationsComponent) },
            { path: 'saved-jobs', loadComponent: () => import('./features/seeker/saved-jobs/saved-jobs').then(m => m.SavedJobsComponent) },
            { path: 'notifications', loadComponent: () => import('./features/seeker/notifications/seeker-notifications').then(m => m.SeekerNotificationsComponent) },
            { path: 'interview-prep', loadComponent: () => import('./features/seeker/interview-prep/interview-prep').then(m => m.InterviewPrepComponent) },
        ]
    },

    // Employer Routes
    {
        path: 'employer',
        canActivate: [authGuard],
        canActivateChild: [authChildGuard],
        children: [
            { path: 'dashboard', loadComponent: () => import('./features/employer/dashboard/employer-dashboard').then(m => m.EmployerDashboardComponent) },
            { path: 'post-job', loadComponent: () => import('./features/employer/post-job/post-job').then(m => m.PostJobComponent) },
            { path: 'edit-job/:id', loadComponent: () => import('./features/employer/post-job/post-job').then(m => m.PostJobComponent) },
            { path: 'jobs', loadComponent: () => import('./features/employer/manage-jobs/manage-jobs').then(m => m.ManageJobsComponent) },
            { path: 'jobs/:id', loadComponent: () => import('./features/employer/job-details/job-details').then(m => m.EmployerJobDetailsComponent) },
            { path: 'applicants', loadComponent: () => import('./features/employer/job-applicants/job-applicants').then(m => m.JobApplicantsComponent) },
            { path: 'seeker/:id', loadComponent: () => import('./features/employer/seeker-profile-view/seeker-profile-view').then(m => m.SeekerProfileViewComponent) },
            { path: 'saved-resumes', loadComponent: () => import('./features/employer/saved-resumes/saved-resumes').then(m => m.SavedResumesComponent) },
            { path: 'company', loadComponent: () => import('./features/employer/company-profile/company-profile').then(m => m.CompanyProfileComponent) },
            { path: 'notifications', loadComponent: () => import('./features/employer/notifications/employer-notifications').then(m => m.EmployerNotificationsComponent) },
        ]
    },

    // Admin Routes
    {
        path: 'admin',
        canActivate: [authGuard, roleGuard],
        data: { roles: ['ADMIN'] },
        children: [
            { path: 'dashboard', loadComponent: () => import('./features/admin/dashboard/admin-dashboard').then(m => m.AdminDashboardComponent) },
        ]
    },

    // Chat
    { path: 'chat', canActivate: [authGuard], loadComponent: () => import('./shared/components/chatbot/chatbot.component').then(m => m.ChatbotComponent) },

    // Error Pages
    { path: '401', loadComponent: () => import('./features/errors/unauthorized/unauthorized').then(m => m.UnauthorizedComponent) },
    { path: '**', loadComponent: () => import('./features/errors/not-found/not-found').then(m => m.NotFoundComponent) }
];
