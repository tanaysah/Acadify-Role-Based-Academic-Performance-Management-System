"""
Main entry point for analytics layer.
Orchestrates data flow: fetch → analyze → report
No SQL queries, no database credentials - pure orchestration.
"""

import sys
from typing import Optional, List, Dict, Any

# Import modules
from db import DatabaseConnection
import queries
from student_analytics import StudentAnalytics
from teacher_analytics import TeacherAnalytics
from subject_analytics import SubjectAnalytics
from reports import ReportGenerator


class AnalyticsOrchestrator:
    """Main orchestrator for analytics operations."""
    
    def __init__(self):
        """Initialize analytics orchestrator."""
        self.db_initialized = False
    
    def initialize(self):
        """Initialize database connection."""
        if not self.db_initialized:
            try:
                DatabaseConnection.initialize(min_conn=2, max_conn=5)
                self.db_initialized = True
                print("[ANALYTICS] System initialized successfully")
            except Exception as e:
                print(f"[ANALYTICS] Initialization failed: {e}")
                sys.exit(1)
    
    def shutdown(self):
        """Shutdown analytics system."""
        DatabaseConnection.close_all()
        print("[ANALYTICS] System shutdown complete")
    
    # ========================================================================
    # STUDENT ANALYTICS WORKFLOWS
    # ========================================================================
    
    def analyze_student(self, student_id: int, generate_report: bool = True) -> Dict[str, Any]:
        """
        Complete student analysis workflow.
        
        Args:
            student_id: Student ID to analyze
            generate_report: Whether to generate text report
            
        Returns:
            Complete student analysis results
        """
        print(f"\n[ANALYTICS] Analyzing student {student_id}...")
        
        # Step 1: Fetch data from database
        print("  [1/4] Fetching student data...")
        performance_data = queries.get_student_performance_report(student_id)
        semester_data = queries.get_semester_performance(student_id)
        marks_trend = queries.get_subject_marks_trend(student_id)
        weak_subjects_data = queries.get_weak_subjects(student_id, threshold=50.0)
        doubts_data = queries.get_student_doubts(student_id)
        
        if not performance_data:
            print(f"  [ERROR] No data found for student {student_id}")
            return {}
        
        # Step 2: Analyze data
        print("  [2/4] Analyzing performance...")
        performance_analysis = StudentAnalytics.analyze_performance_report(performance_data)
        semester_trend = StudentAnalytics.analyze_semester_trend(semester_data)
        subject_performance = StudentAnalytics.analyze_subject_performance(marks_trend)
        improvement_areas = StudentAnalytics.identify_improvement_areas(weak_subjects_data)
        cgpa_prediction = StudentAnalytics.predict_cgpa(semester_data)
        doubt_patterns = StudentAnalytics.analyze_doubt_patterns(doubts_data)
        
        # Step 3: Compile results
        print("  [3/4] Compiling analysis results...")
        results = {
            'student_id': student_id,
            'performance': performance_analysis,
            'semester_trend': semester_trend,
            'subject_analysis': subject_performance,
            'improvement_areas': improvement_areas,
            'cgpa_prediction': cgpa_prediction,
            'doubt_engagement': doubt_patterns
        }
        
        # Step 4: Generate report if requested
        if generate_report:
            print("  [4/4] Generating report...")
            report_content = ReportGenerator.generate_student_report(
                student_id,
                performance_analysis,
                semester_trend,
                subject_performance,
                improvement_areas
            )
            
            filename = f"student_{student_id}_analysis.txt"
            report_path = ReportGenerator.save_report_to_file(report_content, filename)
            results['report_file'] = report_path
        
        print(f"[ANALYTICS] Student {student_id} analysis complete")
        return results
    
    def compare_students(self, student_ids: List[int]) -> Dict[str, Any]:
        """
        Compare multiple students' performance.
        
        Args:
            student_ids: List of student IDs to compare
            
        Returns:
            Comparison results
        """
        print(f"\n[ANALYTICS] Comparing {len(student_ids)} students...")
        
        comparisons = []
        for student_id in student_ids:
            performance = queries.get_student_performance_report(student_id)
            if performance:
                comparisons.append({
                    'student_id': student_id,
                    'name': performance['student_name'],
                    'cgpa': float(performance['current_cgpa']),
                    'average': float(performance['overall_average']),
                    'backlogs': int(performance['total_backlogs'])
                })
        
        # Sort by CGPA
        comparisons.sort(key=lambda x: x['cgpa'], reverse=True)
        
        comparison_data = {
            'total_students': len(comparisons),
            'rankings': comparisons,
            'highest_cgpa': comparisons[0]['cgpa'] if comparisons else 0,
            'lowest_cgpa': comparisons[-1]['cgpa'] if comparisons else 0,
            'average_cgpa': sum(s['cgpa'] for s in comparisons) / len(comparisons) if comparisons else 0
        }
        
        print("[ANALYTICS] Student comparison complete")
        return comparison_data
    
    # ========================================================================
    # TEACHER ANALYTICS WORKFLOWS
    # ========================================================================
    
    def analyze_teacher(self, teacher_id: int, generate_report: bool = True) -> Dict[str, Any]:
        """
        Complete teacher analysis workflow.
        
        Args:
            teacher_id: Teacher ID to analyze
            generate_report: Whether to generate text report
            
        Returns:
            Complete teacher analysis results
        """
        print(f"\n[ANALYTICS] Analyzing teacher {teacher_id}...")
        
        # Step 1: Fetch data
        print("  [1/4] Fetching teacher data...")
        teacher_subjects = queries.get_teacher_subjects(teacher_id)
        teacher_students = queries.get_teacher_students(teacher_id)
        teacher_doubts = queries.get_teacher_doubts(teacher_id)
        
        # Fetch marks for each subject
        subject_marks_map = {}
        for subject in teacher_subjects:
            marks = queries.get_subject_marks(subject['subject_id'], subject['semester'])
            subject_marks_map[subject['subject_id']] = marks
        
        # Step 2: Analyze data
        print("  [2/4] Analyzing performance...")
        performance = TeacherAnalytics.analyze_teacher_performance(teacher_subjects, subject_marks_map)
        student_distribution = TeacherAnalytics.analyze_student_distribution(teacher_students)
        doubt_handling = TeacherAnalytics.analyze_doubt_handling(teacher_doubts)
        
        # Get all marks for struggling student analysis
        all_marks = []
        for marks_list in subject_marks_map.values():
            all_marks.extend(marks_list)
        
        struggling_students = TeacherAnalytics.identify_struggling_students(all_marks)
        grade_distribution = TeacherAnalytics.analyze_grade_distribution(all_marks)
        insights = TeacherAnalytics.generate_teaching_insights(performance, doubt_handling, struggling_students)
        
        # Step 3: Compile results
        print("  [3/4] Compiling analysis results...")
        results = {
            'teacher_id': teacher_id,
            'performance': performance,
            'student_distribution': student_distribution,
            'doubt_handling': doubt_handling,
            'struggling_students': struggling_students,
            'grade_distribution': grade_distribution,
            'teaching_insights': insights
        }
        
        # Step 4: Generate report if requested
        if generate_report:
            print("  [4/4] Generating report...")
            report_content = ReportGenerator.generate_teacher_report(
                teacher_id,
                performance,
                student_distribution,
                doubt_handling,
                insights
            )
            
            filename = f"teacher_{teacher_id}_analysis.txt"
            report_path = ReportGenerator.save_report_to_file(report_content, filename)
            results['report_file'] = report_path
        
        print(f"[ANALYTICS] Teacher {teacher_id} analysis complete")
        return results
    
    # ========================================================================
    # SUBJECT ANALYTICS WORKFLOWS
    # ========================================================================
    
    def analyze_subject(self, subject_id: int, semester: int, generate_report: bool = True) -> Dict[str, Any]:
        """
        Complete subject analysis workflow.
        
        Args:
            subject_id: Subject ID to analyze
            semester: Semester number
            generate_report: Whether to generate text report
            
        Returns:
            Complete subject analysis results
        """
        print(f"\n[ANALYTICS] Analyzing subject {subject_id} (Semester {semester})...")
        
        # Step 1: Fetch data
        print("  [1/4] Fetching subject data...")
        subject_marks = queries.get_subject_marks(subject_id, semester)
        backlog_data = queries.detect_backlogs(semester, passing_marks=40.0)
        
        # Filter backlogs for this subject
        subject_backlogs = [b for b in backlog_data if b['subject_id'] == subject_id]
        
        # Get subject name
        subject_name = subject_marks[0]['subject_name'] if subject_marks else f"Subject {subject_id}"
        
        # Step 2: Analyze data
        print("  [2/4] Analyzing subject metrics...")
        difficulty = SubjectAnalytics.analyze_subject_difficulty(subject_marks, subject_name)
        pass_fail_ratio = SubjectAnalytics.calculate_pass_fail_ratio(subject_marks)
        score_distribution = SubjectAnalytics.analyze_score_distribution(subject_marks)
        backlog_patterns = SubjectAnalytics.analyze_backlog_patterns(subject_backlogs)
        recommendations = SubjectAnalytics.generate_subject_recommendations(difficulty, backlog_patterns)
        
        # Step 3: Compile results
        print("  [3/4] Compiling analysis results...")
        results = {
            'subject_id': subject_id,
            'subject_name': subject_name,
            'semester': semester,
            'difficulty_analysis': difficulty,
            'pass_fail_analysis': pass_fail_ratio,
            'score_distribution': score_distribution,
            'backlog_analysis': backlog_patterns,
            'recommendations': recommendations
        }
        
        # Step 4: Generate report if requested
        if generate_report:
            print("  [4/4] Generating report...")
            report_content = ReportGenerator.generate_subject_report(
                subject_name,
                difficulty,
                score_distribution,
                pass_fail_ratio,
                recommendations
            )
            
            filename = f"subject_{subject_id}_sem{semester}_analysis.txt"
            report_path = ReportGenerator.save_report_to_file(report_content, filename)
            results['report_file'] = report_path
        
        print(f"[ANALYTICS] Subject {subject_id} analysis complete")
        return results
    
    def compare_subjects(self, semester: Optional[int] = None) -> Dict[str, Any]:
        """
        Compare all subjects in a semester or across all semesters.
        
        Args:
            semester: Specific semester or None for all
            
        Returns:
            Subject comparison results
        """
        print(f"\n[ANALYTICS] Comparing subjects (Semester: {semester or 'All'})...")
        
        # Fetch all subjects
        all_subjects = queries.get_all_subjects()
        
        # Filter by semester if specified
        if semester:
            all_subjects = [s for s in all_subjects if s['semester'] == semester]
        
        # Collect marks for each subject
        subjects_data = {}
        for subject in all_subjects:
            marks = queries.get_subject_marks(subject['subject_id'], semester)
            if marks:
                subjects_data[subject['subject_name']] = marks
        
        # Analyze comparison
        comparison = SubjectAnalytics.compare_subjects(subjects_data)
        
        print("[ANALYTICS] Subject comparison complete")
        return comparison
    
    # ========================================================================
    # BATCH ANALYTICS
    # ========================================================================
    
    def analyze_all_students(self, generate_csv: bool = True) -> Dict[str, Any]:
        """
        Analyze all students and generate summary.
        
        Args:
            generate_csv: Whether to generate CSV export
            
        Returns:
            Batch analysis results
        """
        print("\n[ANALYTICS] Batch analyzing all students...")
        
        all_students = queries.get_all_students()
        print(f"  Found {len(all_students)} students")
        
        results = []
        for student in all_students:
            student_id = student['student_id']
            performance = queries.get_student_performance_report(student_id)
            
            if performance:
                analysis = StudentAnalytics.analyze_performance_report(performance)
                results.append({
                    'student_id': student_id,
                    'name': student['name'],
                    'roll_number': student['roll_number'],
                    'cgpa': float(student.get('cgpa', 0)),
                    'performance_category': analysis.get('performance_category', 'Unknown'),
                    'risk_level': analysis.get('risk_level', 'Unknown'),
                    'total_backlogs': analysis.get('total_backlogs', 0)
                })
        
        if generate_csv and results:
            csv_path = ReportGenerator.generate_csv(results, "all_students_analysis.csv")
            print(f"  CSV exported: {csv_path}")
        
        print("[ANALYTICS] Batch student analysis complete")
        return {'total_students': len(results), 'students': results}
    
    def generate_dashboard_overview(self) -> Dict[str, Any]:
        """
        Generate overall system dashboard metrics.
        
        Returns:
            Dashboard overview data
        """
        print("\n[ANALYTICS] Generating dashboard overview...")
        
        # Fetch summary data
        all_students = queries.get_all_students()
        all_teachers = queries.get_all_teachers()
        all_subjects = queries.get_all_subjects()
        all_marks = queries.get_all_marks()
        backlog_data = queries.detect_backlogs()
        
        # Calculate metrics
        total_students = len(all_students)
        total_teachers = len(all_teachers)
        total_subjects = len(all_subjects)
        total_backlogs = len(backlog_data)
        
        avg_cgpa = sum(float(s.get('cgpa', 0)) for s in all_students) / total_students if total_students > 0 else 0
        
        marks_values = [float(m['marks_obtained']) for m in all_marks]
        avg_marks = sum(marks_values) / len(marks_values) if marks_values else 0
        
        pass_count = sum(1 for m in marks_values if m >= 40)
        overall_pass_rate = (pass_count / len(marks_values) * 100) if marks_values else 0
        
        dashboard = {
            'overview': {
                'total_students': total_students,
                'total_teachers': total_teachers,
                'total_subjects': total_subjects,
                'average_cgpa': round(avg_cgpa, 2),
                'average_marks': round(avg_marks, 2),
                'overall_pass_rate': round(overall_pass_rate, 2),
                'total_backlogs': total_backlogs
            }
        }
        
        summary = ReportGenerator.generate_dashboard_summary(dashboard)
        print(summary)
        
        print("[ANALYTICS] Dashboard generation complete")
        return dashboard


def main():
    """Main entry point for analytics CLI."""
    orchestrator = AnalyticsOrchestrator()
    
    try:
        # Initialize system
        orchestrator.initialize()
        
        print("\n" + "="*80)
        print("ACADEMIC ANALYTICS SYSTEM".center(80))
        print("="*80)
        print("\nAvailable Operations:")
        print("  1. Analyze Student")
        print("  2. Analyze Teacher")
        print("  3. Analyze Subject")
        print("  4. Compare Students")
        print("  5. Compare Subjects")
        print("  6. Batch Analyze All Students")
        print("  7. Generate Dashboard Overview")
        print("  0. Exit")
        print("="*80)
        
        while True:
            choice = input("\nSelect operation (0-7): ").strip()
            
            if choice == '0':
                break
            
            elif choice == '1':
                student_id = int(input("Enter Student ID: "))
                results = orchestrator.analyze_student(student_id, generate_report=True)
                print(f"\n[RESULTS] Analysis complete. Report saved at: {results.get('report_file', 'N/A')}")
            
            elif choice == '2':
                teacher_id = int(input("Enter Teacher ID: "))
                results = orchestrator.analyze_teacher(teacher_id, generate_report=True)
                print(f"\n[RESULTS] Analysis complete. Report saved at: {results.get('report_file', 'N/A')}")
            
            elif choice == '3':
                subject_id = int(input("Enter Subject ID: "))
                semester = int(input("Enter Semester: "))
                results = orchestrator.analyze_subject(subject_id, semester, generate_report=True)
                print(f"\n[RESULTS] Analysis complete. Report saved at: {results.get('report_file', 'N/A')}")
            
            elif choice == '4':
                ids_input = input("Enter Student IDs (comma-separated): ")
                student_ids = [int(x.strip()) for x in ids_input.split(',')]
                results = orchestrator.compare_students(student_ids)
                table = ReportGenerator.generate_table(results['rankings'], "Student Comparison")
                print(table)
            
            elif choice == '5':
                semester_input = input("Enter Semester (or press Enter for all): ").strip()
                semester = int(semester_input) if semester_input else None
                results = orchestrator.compare_subjects(semester)
                if results.get('subject_rankings'):
                    table = ReportGenerator.generate_table(results['subject_rankings'], "Subject Comparison")
                    print(table)
            
            elif choice == '6':
                results = orchestrator.analyze_all_students(generate_csv=True)
                print(f"\n[RESULTS] Analyzed {results['total_students']} students. CSV exported.")
            
            elif choice == '7':
                orchestrator.generate_dashboard_overview()
            
            else:
                print("[ERROR] Invalid choice. Please select 0-7.")
    
    except KeyboardInterrupt:
        print("\n\n[INFO] Operation cancelled by user")
    except Exception as e:
        print(f"\n[ERROR] {e}")
    finally:
        orchestrator.shutdown()


if __name__ == "__main__":
    main()
