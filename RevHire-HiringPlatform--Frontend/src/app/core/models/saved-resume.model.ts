import { Resume } from './resume.model';

export interface SavedResume {
    id: number;
    employerId: number;
    seekerId: number;
    seekerName: string;
    seekerEmail: string;
    /** The role (job title) the seeker applied for */
    appliedForRole: string;
    /** The job ID they applied for */
    jobId: number;
    /** A snapshot of the resume submitted at time of application */
    resumeSnapshot: Resume;
    savedAt: string;
}
